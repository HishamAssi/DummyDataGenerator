package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.connectors.DatabaseConnector;
import com.hisham.dummydatagenerator.datasource.DatasourceProvider;
import com.hisham.dummydatagenerator.dto.ConnectionRequest;
import com.hisham.dummydatagenerator.dto.ConnectionRequestAll;
import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.hisham.dummydatagenerator.service.KafkaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UniversalConnectorController is a REST controller that provides endpoints for database operations
 * and data generation. It supports multiple database types through a pluggable connector architecture
 * and can optionally send generated data to Kafka topics.
 *
 * The controller provides three main functionalities:
 * 1. Table introspection - Get metadata about database tables
 * 2. Single table data insertion - Insert dummy data into a specific table
 * 3. Multi-table data insertion - Insert dummy data into multiple tables
 *
 * All operations are thread-safe and support concurrent execution.
 */
@RestController
@RequestMapping("/universal")
public class UniversalConnectorController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalConnectorController.class);

    private final Map<String, KafkaService> kafkaServices = new ConcurrentHashMap<>();
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    private List<DatabaseConnector> connectors;

    @Value("${controller.thread-pool.size:20}")
    private int threadPoolSize;

    public UniversalConnectorController() {
    }

    @PostConstruct
    public void initialize() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(threadPoolSize);
        this.taskExecutor.setMaxPoolSize(threadPoolSize);
        this.taskExecutor.setQueueCapacity(1000);
        this.taskExecutor.setThreadNamePrefix("Connector-");
        this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    /**
     * Introspects a database table and returns its metadata.
     *
     * @param req ConnectionRequest containing database connection details and table information
     * @return TableMetadata object containing the table's structure and constraints
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/introspect")
    public TableMetadata introspect(@RequestBody ConnectionRequest req) {
        DataSource ds = DatasourceProvider.createDataSource(req.getJdbcUrl(), req.getUsername(), req.getPassword());

        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));

        return connector.getTableMetadata(ds, req.getSchema(), req.getTable());
    }

    /**
     * Inserts dummy data into a specified database table.
     * Can optionally send the generated data to a Kafka topic instead of inserting into the database.
     * Supports concurrent operations for improved performance.
     *
     * @param row_count Number of rows to generate per transaction (default: 100)
     * @param tnx Number of transactions to perform (default: 1)
     * @param req ConnectionRequest containing database connection details and optional Kafka configuration
     * @return String message indicating the number of rows and transactions inserted
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/insert")
    public String insert(@RequestParam(defaultValue = "100") int row_count,
                        @RequestParam(defaultValue = "1") int tnx,
                        @RequestBody ConnectionRequest req) {
        DataSource ds = DatasourceProvider.createDataSource(req.getJdbcUrl(), req.getUsername(), req.getPassword());
        
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));
        
        TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), req.getTable());
        int totalRows = 0;

        if (req.getTopic() != null) {
            kafkaServices.computeIfAbsent(req.getTopic(), k -> new KafkaService());
        }

        // Process each transaction sequentially
        for (int i = 0; i < tnx; i++) {
            List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, row_count, req.getSchema());
            if (req.getTopic() != null) {
                KafkaService kafkaService = kafkaServices.get(req.getTopic());
                kafkaService.sendTableData(req.getTopic(), req.getTable(), req.getSchema(), rows, 
                    req.getKafkaConfig().toMap());
            } else {
                connector.insertRows(ds, req.getSchema(), req.getTable(), metadata, rows);
            }
            totalRows += row_count;
        }

        return "Inserted " + tnx + " transaction(s) with " + totalRows + " dummy rows into "
                + metadata.getTableName();
    }

    /**
     * Inserts dummy data into multiple tables in the database.
     * Can optionally send the generated data to a Kafka topic instead of inserting into the database.
     * Supports table inclusion/exclusion lists and handles errors for individual tables gracefully.
     * Uses concurrent processing for improved performance.
     *
     * @param req ConnectionRequestAll containing database connection details, table lists, and optional Kafka configuration
     * @return ResponseEntity containing a map of results with the number of rows inserted per table
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/insert-all")
    public ResponseEntity<Map<String, Object>> insertIntoAllTables(@RequestBody ConnectionRequestAll req) {
        DataSource ds = DatasourceProvider.createDataSource(
                req.getJdbcUrl(), req.getUsername(), req.getPassword());
        
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for: " + req.getDbType()));
        
        List<String> allTables = req.getIncludeTables() != null ? req.getIncludeTables() 
                : connector.getAllTableNames(ds, req.getSchema());
        List<String> toIgnore = req.getIgnoreTables() != null ? req.getIgnoreTables() : List.of();
        Map<String, Integer> resultMap = new ConcurrentHashMap<>();

        if (req.getTopic() != null) {
            kafkaServices.computeIfAbsent(req.getTopic(), k -> new KafkaService());
        }

        List<Future<?>> futures = new ArrayList<>();

        for (String table : allTables) {
            if (toIgnore.contains(table)) {
                logger.debug("[SKIP] Ignoring table: {}", table);
                continue;
            }

            futures.add(taskExecutor.submit(() -> {
                try {
                    TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), table);
                    List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, 
                        req.getRowsPerTable(), req.getSchema());

                    if (req.getTopic() != null) {
                        KafkaService kafkaService = kafkaServices.get(req.getTopic());
                        kafkaService.sendTableData(req.getTopic(), table, req.getSchema(), rows, 
                            req.getKafkaConfig().toMap());
                    } else {
                        connector.insertRows(ds, req.getSchema(), table, metadata, rows);
                    }
                    resultMap.put(table, req.getRowsPerTable());
                } catch (Exception e) {
                    logger.error("[ERROR] Failed to process table: {}", table, e);
                    resultMap.put(table, -1);
                }
            }));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error during batch data insertion", e);
                Thread.currentThread().interrupt();
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Insert complete",
                "rowsInserted", resultMap
        ));
    }

    /**
     * Cleanup method to release resources when the controller is destroyed.
     */
    @PreDestroy
    public void cleanup() {
        kafkaServices.values().forEach(KafkaService::close);
        taskExecutor.shutdown();
    }
}


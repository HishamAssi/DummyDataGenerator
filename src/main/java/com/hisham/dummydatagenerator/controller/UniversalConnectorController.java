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

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;

/**
 * UniversalConnectorController is a REST controller that provides endpoints for database operations
 * and data generation. It supports multiple database types through a pluggable connector architecture
 * and can optionally send generated data to Kafka topics.
 *
 * The controller provides three main functionalities:
 * 1. Table introspection - Get metadata about database tables
 * 2. Single table data insertion - Insert dummy data into a specific table
 * 3. Multi-table data insertion - Insert dummy data into multiple tables
 */
@RestController
@RequestMapping("/universal")
public class UniversalConnectorController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalConnectorController.class);

    private KafkaService kafkaService;

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    private List<DatabaseConnector> connectors;

    /**
     * Introspects a database table and returns its metadata.
     *
     * @param req ConnectionRequest containing database connection details and table information
     * @return TableMetadata object containing the table's structure and constraints
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/introspect")
    public TableMetadata introspect(@RequestBody ConnectionRequest req) {
        // Create datasource from connection details
        DataSource ds = DatasourceProvider.createDataSource(req.getJdbcUrl(), req.getUsername(), req.getPassword());

        // Find appropriate connector for the database type
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));

        return connector.getTableMetadata(ds, req.getSchema(), req.getTable());
    }

    /**
     * Inserts dummy data into a specified database table.
     * Can optionally send the generated data to a Kafka topic instead of inserting into the database.
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
        
        // Find appropriate connector for the database type
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));
        
        // Get table metadata and generate data for specified number of transactions
        TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), req.getTable());
        int tnx_i = 0;
        if (req.getTopic() != null) {
            kafkaService = new KafkaService();
        }
        while (tnx_i < tnx) {
            List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, row_count, req.getSchema());
            if (kafkaService != null) {
                // Send to Kafka if topic is specified and Kafka service is available
                kafkaService.sendTableData(req.getTopic(), req.getTable(), req.getSchema(), rows, req.getKafkaConfig());
            } else {
                // Insert into database if no Kafka topic specified or Kafka service not available
                connector.insertRows(ds, req.getSchema(), req.getTable(), metadata, rows);
            }
            tnx_i++;
        }
        return "Inserted " + tnx + " transaction(s) with " + row_count + " dummy rows into "
                + metadata.getTableName();
    }

    /**
     * Inserts dummy data into multiple tables in the database.
     * Can optionally send the generated data to a Kafka topic instead of inserting into the database.
     * Supports table inclusion/exclusion lists and handles errors for individual tables gracefully.
     *
     * @param req ConnectionRequestAll containing database connection details, table lists, and optional Kafka configuration
     * @return ResponseEntity containing a map of results with the number of rows inserted per table
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/insert-all")
    public ResponseEntity<Map<String, Object>> insertIntoAllTables(@RequestBody ConnectionRequestAll req) {
        DataSource ds = DatasourceProvider.createDataSource(
                req.getJdbcUrl(), req.getUsername(), req.getPassword());
        
        // Find appropriate connector for the database type
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for: " + req.getDbType()));
        
        // Get list of tables to process
        List<String> allTables;
        if (req.getIncludeTables() != null) {
            allTables = req.getIncludeTables();
        } else {
            allTables = connector.getAllTableNames(ds, req.getSchema());
        }
        List<String> toIgnore = req.getIgnoreTables() != null ? req.getIgnoreTables() : List.of();
        Map<String, Integer> resultMap = new LinkedHashMap<>();

        if (req.getTopic() != null) {
            kafkaService = new KafkaService();
        }

        // Process each table
        for (String table : allTables) {
            if (toIgnore.contains(table)) {
                logger.debug("[SKIP] Ignoring table: " + table);
                continue;
            }

            try {
                // Generate and insert data for each table
                TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), table);
                List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, req.getRowsPerTable(),
                        req.getSchema());
                if (kafkaService != null) {
                    // Send to Kafka if topic is specified and Kafka service is available
                    kafkaService.sendTableData(req.getTopic(), req.getSchema(), table, rows, req.getKafkaConfig());
                } else {
                    // Insert into database if no Kafka topic specified or Kafka service not available
                    connector.insertRows(ds, table, req.getSchema(), metadata, rows);
                }
                resultMap.put(table, req.getRowsPerTable());
            } catch (Exception e) {
                if (req.getTopic() != null) {
                    logger.error("[ERROR] Failed to insert into topic: " + req.getDbType(), e);
                }
                else {
                    logger.error("[ERROR] Failed to insert into table: " + table, e);
                }
                resultMap.put(table, -1); // Mark as failed in results
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Insert complete",
                "rowsInserted", resultMap
        ));
    }
}


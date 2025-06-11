package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.connectors.DatabaseConnector;
import com.hisham.dummydatagenerator.datasource.DatasourceProvider;
import com.hisham.dummydatagenerator.dto.ConnectionRequest;
import com.hisham.dummydatagenerator.dto.ConnectionRequestAll;
import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.hisham.dummydatagenerator.service.CSVService;
import com.hisham.dummydatagenerator.service.KafkaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    @Lazy
    private KafkaService kafkaService;

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    @Lazy
    private CSVService csvService;

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
     * @return ResponseEntity containing a map of results with the number of rows inserted and the message
     * @throws RuntimeException if no suitable connector is found for the specified database type
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestParam(defaultValue = "100") int row_count,
                                  @RequestParam(defaultValue = "1") int tnx,
                                  @RequestBody ConnectionRequest req) {
        DataSource ds = DatasourceProvider.createDataSource(req.getJdbcUrl(), req.getUsername(), req.getPassword());
        
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));
        
        TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), req.getTable());
        Map<String, Object> response = new LinkedHashMap<>();
        int totalRows = 0;

        for (int tnx_i = 0; tnx_i < tnx; tnx_i++) {
            List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, row_count, req.getSchema());
            
            if (req.getTopic() != null) {
                kafkaService.sendTableData(req.getTopic(), req.getTable(), req.getSchema(), rows, req.getKafkaConfig());
            } else if (req.isWriteToCSV()) {
                try {
                    Path csvPath = csvService.writeToCSV(req.getSchema(), req.getTable(), rows);
                    response.put("csvFile", csvPath.toString());
                } catch (IOException e) {
                    logger.error("Failed to write CSV file", e);
                    return ResponseEntity.internalServerError().body("Failed to write CSV file: " + e.getMessage());
                }
            } else {
                connector.insertRows(ds, req.getSchema(), req.getTable(), metadata, rows);
            }
            totalRows += rows.size();
        }

        response.put("message", "Successfully processed " + tnx + " transaction(s)");
        response.put("totalRows", totalRows);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> insertIntoAllTables(@RequestBody ConnectionRequestAll req) {
        DataSource ds = DatasourceProvider.createDataSource(
                req.getJdbcUrl(), req.getUsername(), req.getPassword());
        
        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for: " + req.getDbType()));
        
        List<String> allTables = req.getIncludeTables() != null ? req.getIncludeTables() 
                : connector.getAllTableNames(ds, req.getSchema());
        List<String> toIgnore = req.getIgnoreTables() != null ? req.getIgnoreTables() : List.of();
        Map<String, Object> resultMap = new LinkedHashMap<>();
        Map<String, String> csvFiles = new LinkedHashMap<>();

        for (String table : allTables) {
            if (toIgnore.contains(table)) {
                logger.debug("[SKIP] Ignoring table: {}", table);
                continue;
            }

            try {
                TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), table);
                List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, req.getRowsPerTable(),
                        req.getSchema());

                if (req.getTopic() != null) {
                    kafkaService.sendTableData(req.getTopic(), req.getSchema(), table, rows, req.getKafkaConfig());
                } else if (req.isWriteToCSV()) {
                    try {
                        Path csvPath = csvService.writeToCSV(req.getSchema(), table, rows);
                        csvFiles.put(table, csvPath.toString());
                    } catch (IOException e) {
                        logger.error("Failed to write CSV file for table: {}", table, e);
                        resultMap.put(table, Map.of("status", "error", "message", e.getMessage()));
                        continue;
                    }
                } else {
                    connector.insertRows(ds, req.getSchema(), table, metadata, rows);
                }
                resultMap.put(table, Map.of("status", "success", "rows", req.getRowsPerTable()));
            } catch (Exception e) {
                logger.error("[ERROR] Failed to process table: {}", table, e);
                resultMap.put(table, Map.of("status", "error", "message", e.getMessage()));
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Insert complete");
        response.put("results", resultMap);
        if (!csvFiles.isEmpty()) {
            response.put("csvFiles", csvFiles);
        }
        return ResponseEntity.ok(response);
    }
}


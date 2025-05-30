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

@RestController
@RequestMapping("/universal")
public class UniversalConnectorController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalConnectorController.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    private List<DatabaseConnector> connectors;

    @PostMapping("/introspect")
    public TableMetadata introspect(@RequestBody ConnectionRequest req) {
        DataSource ds = DatasourceProvider.createDataSource(req.getJdbcUrl(), req.getUsername(), req.getPassword());

        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for " + req.getDbType()));

        return connector.getTableMetadata(ds, req.getSchema(), req.getTable());
    }

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
        int tnx_i = 0;
        while (tnx_i < tnx) {
            List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, row_count, req.getSchema());
            if (req.getTopic() != null) {
                kafkaService.sendTableData(req.getTopic(), req.getTable(), req.getSchema(), rows, req.getKafkaConfig());
            }
            else {
                connector.insertRows(ds, req.getSchema(), req.getTable(), metadata, rows);
            }
            tnx_i++;
        }
        return "Inserted " + tnx + " transaction(s) with " + row_count + " dummy rows into "
                + metadata.getTableName();
    }

    @PostMapping("/insert-all")
    public ResponseEntity<Map<String, Object>> insertIntoAllTables(@RequestBody ConnectionRequestAll req) {
        DataSource ds = DatasourceProvider.createDataSource(
                req.getJdbcUrl(), req.getUsername(), req.getPassword());

        DatabaseConnector connector = connectors.stream()
                .filter(c -> c.supports(req.getDbType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No connector for: " + req.getDbType()));

        List<String> allTables;
        if (req.getIncludeTables() != null) {
            allTables = req.getIncludeTables();
        }
        else {
            allTables = connector.getAllTableNames(ds, req.getSchema());
        }
        List<String> toIgnore = req.getIgnoreTables() != null ? req.getIgnoreTables() : List.of();
        Map<String, Integer> resultMap = new LinkedHashMap<>();

        for (String table : allTables) {
            if (toIgnore.contains(table)) {
                logger.debug("[SKIP] Ignoring table: " + table);
                continue;
            }

            try {
                TableMetadata metadata = connector.getTableMetadata(ds, req.getSchema(), table);
                List<Map<String, Object>> rows = dummyDataService.generateRows(ds, metadata, req.getRowsPerTable(),
                        req.getSchema());
                if (req.getTopic() != null) {
                    kafkaService.sendTableData(req.getTopic(), req.getSchema(), table, rows, req.getKafkaConfig());
                }
                else {
                    connector.insertRows(ds, table, req.getSchema(), metadata, rows);
                }
                resultMap.put(table, req.getRowsPerTable());
            } catch (Exception e) {
                logger.error("[ERROR] Failed to insert into table: " + table, e);
                resultMap.put(table, 0); // or add error message
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Insert complete",
                "rowsInserted", resultMap
        ));
    }
}


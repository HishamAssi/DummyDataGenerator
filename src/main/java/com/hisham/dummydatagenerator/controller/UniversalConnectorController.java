package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.connectors.DatabaseConnector;
import com.hisham.dummydatagenerator.datasource.DatasourceProvider;
import com.hisham.dummydatagenerator.dto.ConnectionRequest;
import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/universal")
public class UniversalConnectorController {


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
            connector.insertRows(ds, req.getSchema(), req.getTable(), metadata, rows);
            tnx_i++;
        }
        return "Inserted " + tnx + " transaction(s) with " + row_count + " dummy rows into "
                + metadata.getTableName();    }
}


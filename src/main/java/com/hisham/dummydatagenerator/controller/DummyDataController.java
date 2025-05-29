package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.DatabaseIntrospector;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
public class DummyDataController {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    private DatabaseIntrospector introspector;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/{schema}/{table}")
    public String generateData(@PathVariable String schema,
                             @PathVariable String table,
                             @RequestParam(defaultValue = "100") int rows,
                             @RequestParam(defaultValue = "1") int tnx) {

        TableMetadata metadata = introspector.getTableMetadata(schema, table);
        DataSource dataSource = jdbcTemplate.getDataSource();

        int tnx_i = 0;
        int totalRows = 0;

        while (tnx_i < tnx) {
            logger.debug("Inserting transaction number {}", tnx_i);
            List<Map<String, Object>> dummyRows = dummyDataService.generateRows(dataSource, metadata, rows, schema);
            
            
            dummyDataService.insertRows(schema, table, dummyRows);
            
            totalRows += dummyRows.size();
            tnx_i++;
        }

        return String.format(" %d rows for table %s.%s", totalRows, schema, table);
    }

    @GetMapping("/metadata/{schema}/{table}")
    public TableMetadata getTableMetadata(@PathVariable String schema, @PathVariable String table) {
        return introspector.getTableMetadata(schema, table);
    }
}

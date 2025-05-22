package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.DatabaseIntrospector;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{schema}/{table}")
    public String generateData(@PathVariable String schema,
                               @PathVariable String table,
                               @RequestParam(defaultValue = "100") int rows,
                               @RequestParam(defaultValue = "1") int tnx) {

        TableMetadata metadata = introspector.getTableMetadata(schema, table);
        int tnx_i = 0;

        while (tnx_i < tnx) {
            logger.debug("Inserting transaction number {}", tnx_i);
            List<Map<String, Object>> dummyRows = dummyDataService.generateRows(metadata, rows, schema);
            dummyDataService.insertRows(schema, table, dummyRows);
            tnx_i++;

        }
        return "Inserted " + tnx + " transaction(s) with " + rows + " dummy rows into " + schema + "." + table;
    }
}

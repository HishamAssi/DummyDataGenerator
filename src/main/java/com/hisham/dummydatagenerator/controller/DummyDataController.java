package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.generator.DummyDataService;
import com.hisham.dummydatagenerator.schema.DatabaseIntrospector;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
public class DummyDataController {

    @Autowired
    private DummyDataService dummyDataService;

    @Autowired
    private DatabaseIntrospector introspector;

    @PostMapping("/{schema}/{table}")
    public String generateData(@PathVariable String schema,
                               @PathVariable String table,
                               @RequestParam(defaultValue = "100") int rows) {

        TableMetadata metadata = introspector.getTableMetadata(schema, table);
        List<Map<String, Object>> dummyRows = dummyDataService.generateRows(metadata, rows);
        dummyDataService.insertRows(schema, table, dummyRows);

        return "Inserted " + rows + " dummy rows into " + schema + "." + table;
    }
}

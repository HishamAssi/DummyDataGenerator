package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.schema.DatabaseIntrospector;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    @Autowired
    private DatabaseIntrospector introspector;

    @GetMapping("/{schema}/{table}")
    public TableMetadata getTableMetadata(@PathVariable String schema,
                                          @PathVariable String table) {
        return introspector.getTableMetadata(schema, table);
    }
}

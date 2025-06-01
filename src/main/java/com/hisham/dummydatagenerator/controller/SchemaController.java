/**
 * REST controller for database schema operations.
 * Provides endpoints for retrieving table metadata and schema information.
 *
 * This controller is responsible for:
 * - Table structure introspection
 * - Column metadata retrieval
 * - Data type information
 * - Table constraints and relationships
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.controller;

import com.hisham.dummydatagenerator.schema.DatabaseIntrospector;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for schema-related operations.
 * Base path: /schema
 */
@RestController
@RequestMapping("/schema")
public class SchemaController {

    @Autowired
    private DatabaseIntrospector introspector;

    /**
     * Retrieves detailed metadata for a specified table.
     * Returns information about:
     * - Column names and data types
     * - Primary and foreign keys
     * - Constraints and indexes
     * - Default values and nullability
     *
     * @param schema Database schema name
     * @param table Table name
     * @return TableMetadata object containing the table's structure and constraints
     */
    @GetMapping("/{schema}/{table}")
    public TableMetadata getTableMetadata(@PathVariable String schema,
                                          @PathVariable String table) {
        return introspector.getTableMetadata(schema, table);
    }
}

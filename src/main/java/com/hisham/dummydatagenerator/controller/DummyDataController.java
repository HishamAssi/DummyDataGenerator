/**
 * REST controller for generating and managing dummy data in database tables.
 * Provides endpoints for generating synthetic data and retrieving table metadata.
 * Might become obsolete in the future.
 *
 * This controller is designed to work with any JDBC-compliant database and supports:
 * - Configurable number of rows and transactions
 * - Table metadata introspection
 * - Automatic data type-based value generation
 *
 * @author Hisham
 */
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

/**
 * REST controller for dummy data generation operations.
 * Base path: /data
 */
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

    /**
     * Generates and inserts dummy data into a specified table.
     * Supports multiple transactions and configurable number of rows per transaction.
     *
     * @param schema Database schema name
     * @param table Table name
     * @param rows Number of rows to generate per transaction (default: 100)
     * @param tnx Number of transactions to perform (default: 1)
     * @return String message indicating the total number of rows inserted
     */
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

    /**
     * Retrieves metadata for a specified table.
     * Returns information about columns, data types, constraints, and relationships.
     *
     * @param schema Database schema name
     * @param table Table name
     * @return TableMetadata object containing the table's structure
     */
    @GetMapping("/metadata/{schema}/{table}")
    public TableMetadata getTableMetadata(@PathVariable String schema, @PathVariable String table) {
        return introspector.getTableMetadata(schema, table);
    }
}

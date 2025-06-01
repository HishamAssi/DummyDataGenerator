/**
 * Service class for generating and inserting dummy data into database tables.
 * Provides functionality for creating synthetic data based on table metadata and inserting it into the database.
 *
 * This service is responsible for:
 * - Generating rows of dummy data based on column types
 * - Handling primary key uniqueness
 * - Inserting generated data into database tables
 * - Managing database connections and transactions
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Service for generating and managing dummy data.
 * Handles the creation and insertion of synthetic data into database tables.
 */
@Service
public class DummyDataService {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    /**
     * Generates a specified number of rows of dummy data for a table.
     * Ensures primary key uniqueness by checking existing values.
     *
     * @param dataSource DataSource for database connection
     * @param metadata Table metadata containing column information
     * @param rowCount Number of rows to generate
     * @param schema Database schema name
     * @return List of generated rows as maps of column names to values
     */
    public List<Map<String, Object>> generateRows(DataSource dataSource, TableMetadata metadata, int rowCount,
                                                  String schema) {

        logger.info("Generating rows for table {}", metadata.getTableName());
        logger.debug("Row schema: {}", metadata.getColumns());

        List<Map<String, Object>> rows = new ArrayList<>();

        String pkColumn = metadata.getColumns().stream()
                .filter(ColumnMetadata::isPrimaryKey)
                .map(ColumnMetadata::getColumnName)
                .findFirst().orElse(null);

        Set<Object> existingPKs = (pkColumn != null)
                ? fetchExistingPrimaryKeys(dataSource, schema, metadata.getTableName(), pkColumn)
                : Collections.emptySet();

        int generated = 0;
        while (generated < rowCount) {
            Map<String, Object> row = new HashMap<>();

            for (ColumnMetadata column : metadata.getColumns()) {
                logger.debug("Generating column {} for row {}", column.getColumnName(), generated);
                ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column);
                Object value = generator.generate();
                logger.debug("Generated value: {}", value);
                row.put(column.getColumnName(), value);
            }

            if (pkColumn != null) {
                Object pkValue = row.get(pkColumn);
                if (existingPKs.contains(pkValue)) continue;
                existingPKs.add(pkValue);
            }

            rows.add(row);
            generated++;
        }

        return rows;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a list of rows into a specified database table.
     * Uses batch processing for efficient insertion.
     *
     * @param schema Database schema name
     * @param tableName Name of the table to insert into
     * @param rows List of rows to insert, each as a map of column names to values
     */
    public void insertRows(String schema, String tableName, List<Map<String, Object>> rows) {
        logger.debug("Inserting rows into table: {}", tableName);

        if (rows.isEmpty()) return;

        String columns = String.join(", ", rows.get(0).keySet());
        String placeholders = String.join(", ", Collections.nCopies(rows.get(0).size(), "?"));
        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholders);

        for (Map<String, Object> row : rows) {
            jdbcTemplate.update(sql, row.values().toArray());
        }
    }

    /**
     * Fetches existing primary key values from a table.
     * Used to ensure uniqueness of generated primary key values.
     *
     * @param dataSource DataSource for database connection
     * @param schema Database schema name
     * @param tableName Name of the table to query
     * @param primaryKeyColumn Name of the primary key column
     * @return Set of existing primary key values
     * @throws RuntimeException if there is an error accessing the database
     */
    public Set<Object> fetchExistingPrimaryKeys(DataSource dataSource, String schema, String tableName, String primaryKeyColumn) {
        Set<Object> primaryKeys = new HashSet<>();
        String sql = String.format("SELECT %s FROM %s.%s", primaryKeyColumn, schema, tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                primaryKeys.add(rs.getObject(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching primary keys: ", e);
        }

        return primaryKeys;
    }
}

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

@Service
public class DummyDataService {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

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

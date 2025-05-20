package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DummyDataService {

    public List<Map<String, Object>> generateRows(TableMetadata metadata, int rowCount, String schema) {
        List<Map<String, Object>> rows = new ArrayList<>();

        String pkColumn = metadata.getColumns().stream()
                .filter(ColumnMetadata::isPrimaryKey)
                .map(ColumnMetadata::getColumnName)
                .findFirst().orElse(null);

        Set<Object> existingPKs = (pkColumn != null)
                ? fetchExistingPrimaryKeys(schema, metadata.getTableName(), pkColumn)
                : Collections.emptySet();

        int generated = 0;
        while (generated < rowCount) {
            Map<String, Object> row = new HashMap<>();

            for (ColumnMetadata column : metadata.getColumns()) {
                ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column.getDataType());
                Object value = generator.generate();
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
        if (rows.isEmpty()) return;

        String columns = String.join(", ", rows.get(0).keySet());

        String placeholders = String.join(", ", Collections.nCopies(rows.get(0).size(), "?"));

        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholders);

        for (Map<String, Object> row : rows) {
            jdbcTemplate.update(sql, row.values().toArray());
        }
    }

    public Set<Object> fetchExistingPrimaryKeys(String schema, String tableName, String primaryKeyColumn) {
        String sql = String.format("SELECT %s FROM %s.%s", primaryKeyColumn, schema, tableName);
        List<Object> existing = jdbcTemplate.queryForList(sql, Object.class);
        return new HashSet<>(existing);
    }


}

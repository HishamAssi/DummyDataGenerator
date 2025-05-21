package com.hisham.dummydatagenerator.schema;

import javax.sql.DataSource;             // Provided by the JDK or JDBC driver
import java.sql.DatabaseMetaData;       // Part of java.sql, always available
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component; // From spring-context
import org.springframework.beans.factory.annotation.Autowired; // From spring-beans

@Component
public class PostgresDatabaseIntrospector implements DatabaseIntrospector {

    @Autowired
    private DataSource dataSource;

    @Override
    public TableMetadata getTableMetadata(String schema, String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            Set<String> primaryKeys = getPrimaryKeys(schema, tableName, metaData);
            ResultSet columns = metaData.getColumns(null, schema, tableName, null);
            List<ColumnMetadata> columnMetadataList = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME").toLowerCase();
                boolean nullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                boolean isPrimaryKey = primaryKeys.contains(columnName);

                int columnSize = columns.getInt("COLUMN_SIZE");          // e.g., varchar(255) → 255
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");    // e.g., numeric(10,2) → 2

                columnMetadataList.add(new ColumnMetadata(columnName, typeName, nullable, isPrimaryKey, columnSize,
                        decimalDigits));
            }


            return new TableMetadata(tableName, columnMetadataList);

        } catch (SQLException e) {
            throw new RuntimeException("Error reading table metadata", e);
        }
    }

    private Set<String> getPrimaryKeys(String schema, String tableName, DatabaseMetaData metaData) throws SQLException {
        ResultSet pkResultSet = metaData.getPrimaryKeys(null, schema, tableName);
        Set<String> primaryKeys = new HashSet<>();
        while (pkResultSet.next()) {
            primaryKeys.add(pkResultSet.getString("COLUMN_NAME"));
        }
        return primaryKeys;
    }
}

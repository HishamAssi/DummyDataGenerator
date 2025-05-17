package com.hisham.dummydatagenerator.schema;

import javax.sql.DataSource;             // Provided by the JDK or JDBC driver
import java.sql.DatabaseMetaData;       // Part of java.sql, always available
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            ResultSet columns = metaData.getColumns(null, schema, tableName, null);
            List<ColumnMetadata> columnMetadataList = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME");
                boolean nullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                columnMetadataList.add(new ColumnMetadata(columnName, typeName, nullable));
            }

            return new TableMetadata(tableName, columnMetadataList);

        } catch (SQLException e) {
            throw new RuntimeException("Error reading table metadata", e);
        }
    }
}

package com.hisham.dummydatagenerator.connectors;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


@Component
public class PostgresConnector implements DatabaseConnector {

    private static final Logger logger = LoggerFactory.getLogger(PostgresConnector.class);

    public Connection conn;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean supports(String dbType) {
        return dbType.equalsIgnoreCase("postgresql");
    }

    @Override
    public TableMetadata getTableMetadata(DataSource dataSource, String schema, String tableName) {
        List<ColumnMetadata> columns = new ArrayList<>();
        Set<String> primaryKeys = new HashSet<>();

        try (Connection conn = dataSource.getConnection()) {

            logger.debug("Fetching metadata.");

            DatabaseMetaData meta = conn.getMetaData();

            logger.debug("Getting primary keys.");
            ResultSet pkSet = meta.getPrimaryKeys(null, schema, tableName);
            while (pkSet.next()) {
                primaryKeys.add(pkSet.getString("COLUMN_NAME"));
            }

            logger.debug("Getting all columns");
            ResultSet cols = meta.getColumns(null, schema, tableName, null);
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String typeName = cols.getString("TYPE_NAME");
                boolean nullable = cols.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                int size = cols.getInt("COLUMN_SIZE");
                int scale = cols.getInt("DECIMAL_DIGITS");

                ColumnMetadata column = new ColumnMetadata(
                        colName,
                        typeName,
                        nullable,
                        primaryKeys.contains(colName),
                        size,
                        scale
                );
                columns.add(column);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error introspecting Postgres table", e);
        }

        return new TableMetadata(tableName, columns);
    }

    @Override
    public void insertRows(DataSource dataSource, String schema, String tableName, TableMetadata metadata,
                           List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return;

        try (Connection conn = dataSource.getConnection()) {
            List<ColumnMetadata> columns = metadata.getColumns();
            List<String> colNamesList = new ArrayList<>();
            for (ColumnMetadata col : columns) {
                colNamesList.add(col.getColumnName());
            }

            String colNames = String.join(", ", colNamesList);
            String placeholders = String.join(", ", Collections.nCopies(colNamesList.size(), "?"));
            String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, colNames, placeholders);

            logger.debug("Prepared insert SQL: {}", sql);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map<String, Object> row : rows) {
                    for (int i = 0; i < columns.size(); i++) {
                        Object value = row.get(columns.get(i).getColumnName());
                        stmt.setObject(i + 1, value);
                    }
                    int insertResult = stmt.executeUpdate();
                    logger.debug("Insert returned code: {}", insertResult);
                }
            }

        } catch (SQLException e) {
            logger.error("Error inserting rows into table {}.{}", schema, tableName, e);
            throw new RuntimeException("Insert failed", e);
        }
    }

    public List<String> getAllTableNames(DataSource ds, String schema) {
        List<String> tables = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, schema, null, new String[] { "TABLE" });
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tables for schema: " + schema, e);
        }

        return tables;
    }

    public void createTable(DataSource dataSource, String statement, String tableName, String schema) {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }

}

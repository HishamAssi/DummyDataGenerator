package com.hisham.dummydatagenerator.connectors;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * RelationalDatabaseConnector is a concrete implementation of the DatabaseConnector interface
 * that provides functionality for interacting with relational databases (PostgreSQL and SQL Server).
 * 
 * This connector handles:
 * - Table metadata retrieval (columns, primary keys, data types)
 * - Data insertion with prepared statements
 * - Table existence checks
 * - Table creation
 * - Schema introspection
 * 
 * The connector uses JDBC under the hood and implements thread-safe operations.
 * All database operations are performed using try-with-resources to ensure proper resource cleanup.
 */
@Component
public class RelationalDatabaseConnector implements DatabaseConnector {

    private static final Logger logger = LoggerFactory.getLogger(RelationalDatabaseConnector.class);
    
    /**
     * Set of supported database types. Currently supports:
     * - PostgreSQL
     * - SQL Server
     */
    private static final Set<String> SUPPORTED_DB_TYPES = Set.of("postgresql", "sqlserver");

    /**
     * Checks if the connector supports the specified database type.
     * 
     * @param dbType The database type to check (case-insensitive)
     * @return true if the database type is supported, false otherwise
     */
    @Override
    public boolean supports(String dbType) {
        return SUPPORTED_DB_TYPES.contains(dbType.toLowerCase());
    }

    /**
     * Retrieves metadata for a specific table, including column information and primary keys.
     * 
     * @param dataSource The database connection source
     * @param schema The database schema name
     * @param tableName The name of the table to introspect
     * @return TableMetadata object containing column definitions and primary key information
     * @throws RuntimeException if there's an error accessing the database metadata
     */
    @Override
    public TableMetadata getTableMetadata(DataSource dataSource, String schema, String tableName) {
        List<ColumnMetadata> columns = new ArrayList<>();
        Set<String> primaryKeys = new HashSet<>();

        try (Connection conn = dataSource.getConnection()) {
            logger.debug("Fetching metadata for table {}.{}", schema, tableName);

            DatabaseMetaData meta = conn.getMetaData();

            // First, get all primary keys for the table
            logger.debug("Retrieving primary keys for table {}.{}", schema, tableName);
            ResultSet pkSet = meta.getPrimaryKeys(null, schema, tableName);
            while (pkSet.next()) {
                primaryKeys.add(pkSet.getString("COLUMN_NAME"));
            }

            // Then, get all column information
            logger.debug("Retrieving column information for table {}.{}", schema, tableName);
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
            logger.error("Error introspecting table {}.{}", schema, tableName, e);
            throw new RuntimeException("Error introspecting table", e);
        }

        return new TableMetadata(tableName, columns);
    }

    /**
     * Inserts multiple rows into a specified table using batch processing.
     * Uses prepared statements for safe parameter binding and better performance.
     * 
     * @param dataSource The database connection source
     * @param schema The database schema name
     * @param tableName The target table name
     * @param metadata The table metadata containing column information
     * @param rows List of maps containing column name to value mappings for each row
     * @throws RuntimeException if there's an error during the insert operation
     */
    @Override
    public void insertRows(DataSource dataSource, String schema, String tableName, TableMetadata metadata,
                           List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            logger.debug("No rows to insert into {}.{}", schema, tableName);
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // Prepare column names and placeholders for the SQL statement
            List<ColumnMetadata> columns = metadata.getColumns();
            List<String> colNamesList = new ArrayList<>();
            for (ColumnMetadata col : columns) {
                colNamesList.add(col.getColumnName());
            }

            String colNames = String.join(", ", colNamesList);
            String placeholders = String.join(", ", Collections.nCopies(colNamesList.size(), "?"));
            String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, colNames, placeholders);

            logger.debug("Executing insert with SQL: {}", sql);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map<String, Object> row : rows) {
                    // Set values for each column in the prepared statement
                    for (int i = 0; i < columns.size(); i++) {
                        Object value = row.get(columns.get(i).getColumnName());
                        stmt.setObject(i + 1, value);
                    }
                    int insertResult = stmt.executeUpdate();
                    logger.trace("Insert completed with result code: {}", insertResult);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to insert rows into table {}.{}", schema, tableName, e);
            throw new RuntimeException("Insert failed", e);
        }
    }

    /**
     * Retrieves a list of all table names in the specified schema.
     * 
     * @param ds The database connection source
     * @param schema The database schema name
     * @return List of table names in the schema
     * @throws RuntimeException if there's an error accessing the database metadata
     */
    @Override
    public List<String> getAllTableNames(DataSource ds, String schema) {
        List<String> tables = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, schema, null, new String[] { "TABLE" });
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch tables for schema: {}", schema, e);
            throw new RuntimeException("Failed to fetch tables for schema: " + schema, e);
        }

        return tables;
    }

    /**
     * Creates a new table in the database if it doesn't already exist.
     * 
     * @param dataSource The database connection source
     * @param statement The SQL CREATE TABLE statement
     * @param tableName The name of the table to create
     * @param schema The database schema name
     * @throws RuntimeException if there's an error creating the table
     */
    @Override
    public void createTable(DataSource dataSource, String statement, String tableName, String schema) {
        try (Connection conn = dataSource.getConnection()) {
            // Check if table already exists
            if (tableExists(conn, schema, tableName)) {
                logger.info("Table {}.{} already exists, skipping creation", schema, tableName);
                return;
            }

            // Execute the create table statement
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(statement);
                logger.info("Successfully created table {}.{}", schema, tableName);
            }
        } catch (SQLException e) {
            logger.error("Failed to create table {}.{}", schema, tableName, e);
            throw new RuntimeException("Failed to create table", e);
        }
    }

    /**
     * Checks if a table exists in the specified schema.
     * 
     * @param conn The database connection
     * @param schema The database schema name
     * @param tableName The name of the table to check
     * @return true if the table exists, false otherwise
     * @throws SQLException if there's an error querying the database
     */
    public boolean tableExists(Connection conn, String schema, String tableName) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
} 
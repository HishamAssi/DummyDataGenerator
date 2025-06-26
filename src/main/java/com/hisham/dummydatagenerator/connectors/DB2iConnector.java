package com.hisham.dummydatagenerator.connectors;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JDBCConnection;
import com.ibm.as400.access.AS400JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * DB2iConnector is a specialized implementation of DatabaseConnector for IBM i (AS/400) systems.
 * It uses the JT400 library to interact with DB2 on i databases.
 * 
 * This connector handles:
 * - Table metadata retrieval specific to DB2 on i
 * - Data insertion with prepared statements
 * - Table existence checks using DB2 on i specific catalog views
 * - Table creation with DB2 on i specific syntax
 * - Schema introspection using DB2 on i system catalogs
 */
@Component
public class DB2iConnector implements DatabaseConnector {

    private static final Logger logger = LoggerFactory.getLogger(DB2iConnector.class);
    private static final Set<String> SUPPORTED_DB_TYPES = Set.of("db2i", "as400");

    @Override
    public boolean supports(String dbType) {
        return SUPPORTED_DB_TYPES.contains(dbType.toLowerCase());
    }

    @Override
    public TableMetadata getTableMetadata(DataSource dataSource, String schema, String tableName) {
        List<ColumnMetadata> columns = new ArrayList<>();
        Set<String> primaryKeys = new HashSet<>();

        try (Connection conn = dataSource.getConnection()) {
            logger.debug("Fetching metadata for table {}.{}", schema, tableName);

            // Get primary keys using DB2 on i specific catalog
            logger.debug("Retrieving primary keys for table {}.{}", schema, tableName);
            String pkQuery = """
                SELECT COLUMN_NAME 
                FROM QSYS2.SYSKEYCST 
                WHERE TABLE_SCHEMA = ? 
                AND TABLE_NAME = ? 
                AND CONSTRAINT_TYPE = 'P'
                """;
            
            try (PreparedStatement pkStmt = conn.prepareStatement(pkQuery)) {
                pkStmt.setString(1, schema);
                pkStmt.setString(2, tableName);
                ResultSet pkSet = pkStmt.executeQuery();
                while (pkSet.next()) {
                    primaryKeys.add(pkSet.getString("COLUMN_NAME"));
                }
            }

            // Get column information using DB2 on i specific catalog
            logger.debug("Retrieving column information for table {}.{}", schema, tableName);
            String colQuery = """
                SELECT COLUMN_NAME, DATA_TYPE, COLUMN_SIZE, DECIMAL_DIGITS, 
                       IS_NULLABLE, COLUMN_DEFAULT
                FROM QSYS2.SYSCOLUMNS 
                WHERE TABLE_SCHEMA = ? 
                AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;

            try (PreparedStatement colStmt = conn.prepareStatement(colQuery)) {
                colStmt.setString(1, schema);
                colStmt.setString(2, tableName);
                ResultSet cols = colStmt.executeQuery();
                
                while (cols.next()) {
                    String colName = cols.getString("COLUMN_NAME");
                    String typeName = cols.getString("DATA_TYPE");
                    boolean nullable = "YES".equals(cols.getString("IS_NULLABLE"));
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
            }

        } catch (SQLException e) {
            logger.error("Error introspecting table {}.{}", schema, tableName, e);
            throw new RuntimeException("Error introspecting table", e);
        }

        return new TableMetadata(tableName, columns);
    }

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
                        // Handle DB2 on i specific data type conversions if needed
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

    @Override
    public List<String> getAllTableNames(DataSource ds, String schema) {
        List<String> tables = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            String sql = """
                SELECT TABLE_NAME 
                FROM QSYS2.SYSTABLES 
                WHERE TABLE_SCHEMA = ? 
                AND TABLE_TYPE = 'T'
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, schema);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch tables for schema: {}", schema, e);
            throw new RuntimeException("Failed to fetch tables for schema: " + schema, e);
        }

        return tables;
    }

    @Override
    public void createTable(DataSource dataSource, String statement, String tableName, String schema) {
        try (Connection conn = dataSource.getConnection()) {
            // Check if table already exists using DB2 on i specific catalog
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

    public boolean tableExists(Connection conn, String schema, String tableName) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM QSYS2.SYSTABLES 
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
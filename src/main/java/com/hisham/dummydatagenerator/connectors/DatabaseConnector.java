package com.hisham.dummydatagenerator.connectors;

import com.hisham.dummydatagenerator.schema.TableMetadata;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List; 
import java.util.Map;

public interface DatabaseConnector {

    boolean supports(String dbType); // e.g., "oracle", "snowflake"

    TableMetadata getTableMetadata(DataSource dataSource, String schema, String tableName);

    void insertRows(DataSource dataSource, String schema, String tableName, TableMetadata metadata,
                    List<Map<String, Object>> rows);

    List<String> getAllTableNames(DataSource dataSource, String schema);

    void createTable(DataSource dataSource, String statement, String tableName, String schema);

    boolean tableExists(Connection conn, String schema, String tableName) throws SQLException;

}

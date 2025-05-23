package com.hisham.dummydatagenerator.connectors;

import com.hisham.dummydatagenerator.schema.TableMetadata;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public interface DatabaseConnector {

    boolean supports(String dbType); // e.g., "oracle", "snowflake"

    TableMetadata getTableMetadata(DataSource dataSource, String schema, String tableName);

    void insertRows(DataSource dataSource, String schema, String tableName, TableMetadata metadata,
                    List<Map<String, Object>> rows);

}

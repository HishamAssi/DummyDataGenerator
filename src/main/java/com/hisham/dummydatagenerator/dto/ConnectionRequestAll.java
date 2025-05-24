package com.hisham.dummydatagenerator.dto;

import java.util.List;

public class ConnectionRequestAll {
    private String dbType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String schema;
    private int rowsPerTable;

    private List<String> ignoreTables;

    private List<String> includeTables;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }

    public int getRowsPerTable() {
        return rowsPerTable;
    }

    public List<String> getIgnoreTables() {
        return ignoreTables;
    }

    public List<String> getIncludeTables() {
        return includeTables;
    }
}


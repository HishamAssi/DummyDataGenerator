package com.hisham.dummydatagenerator.dto;

public class ConnectionRequest {
    private String dbType;              // "sqlserver", "oracle", etc.
    private String jdbcUrl;
    private String username;
    private String password;
    private String schema;
    private String table;

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

    public String getTable() {
        return table;
    }
}


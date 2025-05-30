package com.hisham.dummydatagenerator.dto;

import java.util.List;

public class ConnectionRequestAll {
    private String dbType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String schema;
    private int rowsPerTable;
    private String topic;
    private KafkaProducerConfig kafkaConfig;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setRowsPerTable(int rowsPerTable) {
        this.rowsPerTable=rowsPerTable;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public KafkaProducerConfig getKafkaConfig() {
        return kafkaConfig;
    }

    public void setKafkaConfig(KafkaProducerConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }
}


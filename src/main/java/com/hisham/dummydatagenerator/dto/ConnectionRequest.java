package com.hisham.dummydatagenerator.dto;

import lombok.Data;

@Data
public class ConnectionRequest {
    private String jdbcUrl;
    private String username;
    private String password;
    private String dbType;
    private String schema;
    private String table;
    private String topic;
    private KafkaProducerConfig kafkaConfig;
    private boolean writeToCSV;
    private String csvOutputDir;
    private Boolean includeHeader;

    // Getters and Setters
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public KafkaProducerConfig getKafkaConfig() {
        return kafkaConfig;
    }

    public void setKafkaConfig(KafkaProducerConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }
}


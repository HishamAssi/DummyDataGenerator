package com.hisham.dummydatagenerator.dto;

import java.util.Map;

public class TableDataMessage {
    private String tableName;
    private String schema;
    private Map<String, Object> data;

    public TableDataMessage() {
    }

    public TableDataMessage(String tableName, String schema, Map<String, Object> data) {
        this.tableName = tableName;
        this.schema = schema;
        this.data = data;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
} 
package com.hisham.dummydatagenerator.schema;

import java.util.List;

public class TableMetadata {
    private String tableName;
    private List<ColumnMetadata> columns;

    public TableMetadata(String tableName, List<ColumnMetadata> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    // Getters
    public String getTableName() {
        return tableName;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

}

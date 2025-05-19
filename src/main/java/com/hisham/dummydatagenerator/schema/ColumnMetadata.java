package com.hisham.dummydatagenerator.schema;

public class ColumnMetadata {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private boolean primaryKey;

    public ColumnMetadata(String columnName, String dataType, boolean nullable, boolean primaryKey) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
    }

    // Getters and setters
    public String getColumnName() {
        return columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}

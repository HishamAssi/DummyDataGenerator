package com.hisham.dummydatagenerator.schema;

public class ColumnMetadata {
    private String columnName;
    private String dataType;
    private boolean nullable;

    public ColumnMetadata(String columnName, String dataType, boolean nullable) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
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
}

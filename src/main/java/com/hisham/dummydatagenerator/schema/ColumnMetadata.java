package com.hisham.dummydatagenerator.schema;

public class ColumnMetadata {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private boolean primaryKey;

    private Integer columnSize;     // for varchar, char, numeric
    private Integer decimalDigits;  // for numeric

    public ColumnMetadata(String columnName, String dataType, boolean nullable, boolean primaryKey,
                          Integer columnSize, Integer decimalDigits) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
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

    public Integer getColumnSize() { return columnSize; }

    public Integer getDecimalDigits() { return decimalDigits; }

}

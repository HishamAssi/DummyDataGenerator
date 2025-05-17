package com.hisham.dummydatagenerator.schema;

public interface DatabaseIntrospector {
    TableMetadata getTableMetadata(String schema, String tableName);
}

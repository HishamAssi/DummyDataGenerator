package com.hisham.dummydatagenerator.generator;

import java.util.UUID;

public class UUIDGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return UUID.randomUUID().toString();
    }
}

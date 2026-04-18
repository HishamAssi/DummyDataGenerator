package com.hisham.dummydatagenerator.generator;

import java.util.UUID;

public class JsonGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return "{\"key\":\"val_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "\"}";
    }
}

package com.hisham.dummydatagenerator.generator;

import java.util.UUID;

public class StringGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return "name_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
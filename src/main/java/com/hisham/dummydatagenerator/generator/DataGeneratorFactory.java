package com.hisham.dummydatagenerator.generator;

import java.util.HashMap;
import java.util.Map;

public class DataGeneratorFactory {

    private static final Map<String, ColumnDataGenerator> generatorMap = new HashMap<>();

    static {
        generatorMap.put("varchar", new StringGenerator());
        generatorMap.put("text", new StringGenerator());
        generatorMap.put("int4", new IntegerGenerator()); // PostgreSQL-specific
        generatorMap.put("integer", new IntegerGenerator());
        generatorMap.put("date", new DateGenerator());
        // add more mappings as needed
    }

    public static ColumnDataGenerator getGenerator(String sqlType) {
        return generatorMap.getOrDefault(sqlType.toLowerCase(), () -> null);
    }
}

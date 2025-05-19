package com.hisham.dummydatagenerator.generator;

import java.util.concurrent.ThreadLocalRandom;

public class IntegerGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return ThreadLocalRandom.current().nextInt(1, 10000);
    }
}

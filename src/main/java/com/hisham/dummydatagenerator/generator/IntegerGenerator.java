package com.hisham.dummydatagenerator.generator;

import java.util.concurrent.ThreadLocalRandom;

public class IntegerGenerator implements ColumnDataGenerator {

    private int min;
    private int max;

    public IntegerGenerator(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Object generate() {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}

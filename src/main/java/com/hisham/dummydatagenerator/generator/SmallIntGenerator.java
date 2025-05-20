package com.hisham.dummydatagenerator.generator;

import java.util.concurrent.ThreadLocalRandom;

public class SmallIntGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return (short) ThreadLocalRandom.current().nextInt(-32768, 32767);
    }
}


package com.hisham.dummydatagenerator.generator;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VarcharGenerator implements ColumnDataGenerator {
    private final int maxLength;

    public VarcharGenerator(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public Object generate() {
        String base = "val_" + UUID.randomUUID().toString().replace("-", "");
        return base.substring(0, Math.min(base.length(), ThreadLocalRandom.current().nextInt(1,
                maxLength+1)));
    }
}

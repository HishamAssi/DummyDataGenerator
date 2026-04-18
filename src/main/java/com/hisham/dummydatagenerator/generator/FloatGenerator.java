package com.hisham.dummydatagenerator.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class FloatGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(-1_000_000, 1_000_000))
                .setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}

package com.hisham.dummydatagenerator.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class NumericGenerator implements ColumnDataGenerator {
        private final int precision;
        private final int scale;

    public NumericGenerator(Integer precision, Integer scale) {
            this.precision = (precision != null) ? precision : 10;
            this.scale = (scale != null) ? scale : 2;
        }

        @Override
        public Object generate() {
            double max = Math.pow(10, precision - scale) - 1;
            double value = ThreadLocalRandom.current().nextDouble(0, max);
            return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
        }
    }

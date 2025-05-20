package com.hisham.dummydatagenerator.generator;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class DateGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        long minDay = LocalDate.of(2000, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2022, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }
}

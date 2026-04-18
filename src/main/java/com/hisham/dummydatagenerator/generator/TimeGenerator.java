package com.hisham.dummydatagenerator.generator;

import java.sql.Time;
import java.util.concurrent.ThreadLocalRandom;

public class TimeGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        int hour = ThreadLocalRandom.current().nextInt(0, 24);
        int minute = ThreadLocalRandom.current().nextInt(0, 60);
        int second = ThreadLocalRandom.current().nextInt(0, 60);
        return Time.valueOf(String.format("%02d:%02d:%02d", hour, minute, second));
    }
}

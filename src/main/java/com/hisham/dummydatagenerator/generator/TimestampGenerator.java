package com.hisham.dummydatagenerator.generator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

public class TimestampGenerator implements ColumnDataGenerator {



    @Override
    public Object generate() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(ThreadLocalRandom.current().nextInt(0, 365));
    }
}


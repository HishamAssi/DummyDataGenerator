package com.hisham.dummydatagenerator.generator;

import java.util.concurrent.ThreadLocalRandom;

public class BooleanGenerator implements ColumnDataGenerator {
    @Override
    public Object generate(){
        return ThreadLocalRandom.current().nextBoolean();
    }
}

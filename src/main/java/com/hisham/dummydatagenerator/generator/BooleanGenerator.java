package com.hisham.dummydatagenerator.generator;

import java.util.Random;

public class BooleanGenerator implements ColumnDataGenerator {
    @Override
    public Object generate(){
        Random random = new Random();
        boolean randomBoolean = random.nextBoolean();
        return randomBoolean;
    }
}

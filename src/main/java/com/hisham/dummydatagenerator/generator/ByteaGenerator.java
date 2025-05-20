package com.hisham.dummydatagenerator.generator;

import java.util.concurrent.ThreadLocalRandom;

public class ByteaGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        byte[] data = new byte[16];
        ThreadLocalRandom.current().nextBytes(data);
        return data;
    }
}


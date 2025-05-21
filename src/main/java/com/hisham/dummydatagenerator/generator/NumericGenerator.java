package com.hisham.dummydatagenerator.generator;

import java.util.Random;
import java.util.UUID;

public class NumericGenerator implements ColumnDataGenerator {
    private int numericSize;
    private int numericDecimalDigits;

    public NumericGenerator(Integer numericSize, Integer numericDecimalDigits){
        this.numericSize = numericSize;
        this.numericDecimalDigits = numericDecimalDigits;
    }

    @Override
    public Object generate() {
        double max = Math.pow(10, numericSize);
        double randomDouble = new Random().nextDouble() * max;

        double scale = Math.pow(10, numericDecimalDigits);
        return (float) (Math.round(randomDouble * scale) / scale);
    }



}

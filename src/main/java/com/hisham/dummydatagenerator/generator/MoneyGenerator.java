package com.hisham.dummydatagenerator.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class MoneyGenerator implements ColumnDataGenerator {
    /*
    From postgres docs:
    ---
    Name	| Storage Size	| Description	    | Range
    money	| 8 bytes	    | currency amount	| -92233720368547758.08 to +92233720368547758.07
    ---
     */
    @Override
    public Object generate() {
        return "$" + BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10, 500)).setScale(2, RoundingMode.HALF_UP);
    }
}


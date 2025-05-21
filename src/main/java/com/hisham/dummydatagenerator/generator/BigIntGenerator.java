package com.hisham.dummydatagenerator.generator;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class BigIntGenerator implements ColumnDataGenerator {
    @Override
    public Object generate() {
        BigInteger min = new BigInteger("-9223372036854775807");
        BigInteger max = new BigInteger("9223372036854775807");
        return generateRandomBigInteger(min,max);
    }

    public static BigInteger generateRandomBigInteger(BigInteger min, BigInteger max) {
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException("min must be less than max");
        }
        BigInteger range = max.subtract(min);
        int numBits = range.bitLength();
        BigInteger randomInRange;

        do {
            randomInRange = new BigInteger(numBits, new SecureRandom());
        } while (randomInRange.compareTo(range) >= 0);

        return randomInRange.add(min);
    }
}


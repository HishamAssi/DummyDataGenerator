package com.hisham.dummydatagenerator.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class VarcharGeneratorTest {

    @Test
    void testGenerateWithDefaultLength() {
        VarcharGenerator generator = new VarcharGenerator(50);
        String result = (String) generator.generate();
        
        assertNotNull(result);
        assertTrue(result.length() <= 50);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1000})
    void testGenerateWithDifferentLengths(int maxLength) {
        VarcharGenerator generator = new VarcharGenerator(maxLength);
        String result = (String) generator.generate();
        
        assertNotNull(result);
        assertTrue(result.length() <= maxLength);
    }

    @Test
    void testGenerateMultipleValuesAreDifferent() {
        VarcharGenerator generator = new VarcharGenerator(50);
        String result1 = (String) generator.generate();
        String result2 = (String) generator.generate();
        
        assertNotEquals(result1, result2, "Generated values should be different");
    }

    @Test
    void testGenerateWithMinimumLength() {
        VarcharGenerator generator = new VarcharGenerator(1);
        String result = (String) generator.generate();
        
        assertNotNull(result);
        assertTrue(result.length() >= 1);
        assertTrue(result.length() <= 1);
    }
} 
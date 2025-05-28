package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.*;

class DataGeneratorFactoryTest {

    @ParameterizedTest
    @CsvSource({
        "varchar, 50, , VarcharGenerator",
        "text, , , VarcharGenerator",
        "numeric, 10, 2, NumericGenerator",
        "int2, , , IntegerGenerator",
        "int4, , , IntegerGenerator",
        "int8, , , BigIntGenerator",
        "timestamptz, , , TimestampGenerator",
        "timestamp, , , TimestampGenerator",
        "bool, , , BooleanGenerator",
        "bytea, , , ByteaGenerator",
        "date, , , DateGenerator"
    })
    void testGetGeneratorForKnownTypes(String type, Integer size, Integer scale, String expectedGeneratorClass) {
        ColumnMetadata column = new ColumnMetadata("test_column", type, true, false, size, scale);
        ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column);
        
        assertNotNull(generator);
        assertEquals(expectedGeneratorClass, generator.getClass().getSimpleName());
    }

    @Test
    void testGetGeneratorForUnknownType() {
        ColumnMetadata column = new ColumnMetadata("test_column", "unknown_type", true, false, null, null);
        ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column);
        
        assertNotNull(generator);
        assertNull(generator.generate());
    }

    @Test
    void testGetGeneratorWithNullColumn() {
        assertThrows(NullPointerException.class, () -> {
            DataGeneratorFactory.getGenerator(null);
        });
    }

    @Test
    void testGetGeneratorWithNullType() {
        ColumnMetadata column = new ColumnMetadata("test_column", null, true, false, null, null);
        ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column);
        
        assertNotNull(generator);
        assertNull(generator.generate());
    }
} 
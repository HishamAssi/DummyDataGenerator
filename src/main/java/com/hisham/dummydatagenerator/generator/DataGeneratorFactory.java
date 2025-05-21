package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;

import java.util.HashMap;
import java.util.Map;

public class DataGeneratorFactory {

    /*
    generatorMap.put("int2", new SmallIntGenerator());
generatorMap.put("int4", new IntegerGenerator());
generatorMap.put("int8", new BigIntGenerator());
generatorMap.put("numeric", new BigDecimalGenerator());
generatorMap.put("money", new MoneyGenerator());
generatorMap.put("char", new FixedLengthStringGenerator(10));
generatorMap.put("varchar", new StringGenerator());
generatorMap.put("text", new LongTextGenerator());
generatorMap.put("bytea", new ByteaGenerator());
generatorMap.put("date", new DateGenerator());
generatorMap.put("time", new TimeGenerator());
generatorMap.put("timestamp", new TimestampGenerator());
generatorMap.put("timestamptz", new TimestampTzGenerator());
generatorMap.put("interval", new IntervalGenerator());
generatorMap.put("bool", new BooleanGenerator());
*/

    public static ColumnDataGenerator getGenerator(ColumnMetadata column) {
        String type = column.getDataType().toLowerCase();

        return switch (type) {
            case "varchar" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 50);
            case "numeric" -> new NumericGenerator(column.getColumnSize(), column.getDecimalDigits());
            case "int2" -> new SmallIntGenerator();
            case "int4" -> new IntegerGenerator();
            case "int8" -> new BigIntGenerator();
            case "money" -> new MoneyGenerator();
            case "timestamptz" -> new TimestampTzGenerator();
            case "bool" -> new BooleanGenerator();
            case "bytea" -> new ByteaGenerator();
            case "date" -> new DateGenerator();
            // Add other mappings here
            default -> () -> null;
        };
    }
}

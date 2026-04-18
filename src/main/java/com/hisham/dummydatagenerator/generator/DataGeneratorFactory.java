/**
 * Factory class for creating data generators based on column metadata.
 * Provides appropriate data generators for different SQL data types.
 *
 * This factory is responsible for:
 * - Mapping SQL data types to appropriate generators
 * - Creating generators with correct size and precision constraints
 * - Handling null cases and unknown data types
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.ColumnMetadata;

/**
 * Factory for creating data generators based on column metadata.
 * Maps SQL data types to appropriate generator implementations.
 */
public class DataGeneratorFactory {

    /** Maximum value for smallint (int2) type */
    private final static int smallInt = 32768;
    /** Maximum value for integer (int4) type */
    private final static int normInt = 2147483647;
    /** Maximum size for text type */
    private final static int textSize = normInt - 1;

    /*
     * Supported data type mappings:
     * - int2 -> SmallIntGenerator
     * - int4 -> IntegerGenerator
     * - int8 -> BigIntGenerator
     * - numeric -> BigDecimalGenerator
     * - money -> MoneyGenerator
     * - char -> FixedLengthStringGenerator
     * - varchar -> StringGenerator
     * - text -> LongTextGenerator
     * - bytea -> ByteaGenerator
     * - date -> DateGenerator
     * - time -> TimeGenerator
     * - timestamp -> TimestampGenerator
     * - timestamptz -> TimestampTzGenerator
     * - interval -> IntervalGenerator
     * - bool -> BooleanGenerator
     */

    /**
     * Creates an appropriate data generator for the given column metadata.
     * Returns a generator that produces values matching the column's data type and constraints.
     *
     * @param column Column metadata containing data type and constraints
     * @return ColumnDataGenerator instance appropriate for the column's data type
     * @throws NullPointerException if column metadata is null
     */
    public static ColumnDataGenerator getGenerator(ColumnMetadata column) {
        if (column == null) {
            throw new NullPointerException("Column metadata cannot be null");
        }

        String type = column.getDataType();
        if (type == null) {
            return () -> null;
        }

        return switch (type.toLowerCase()) {
            case "varchar" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 50);
            case "text" -> new VarcharGenerator(textSize);
            case "numeric", "decimal" -> new NumericGenerator(column.getColumnSize(), column.getDecimalDigits());
            case "int2" -> new IntegerGenerator(-smallInt, smallInt);
            case "int4", "int"  -> new IntegerGenerator(-normInt, normInt);
            case "money" -> new MoneyGenerator();
            case "int8" -> new BigIntGenerator();
            case "timestamptz" -> new TimestampGenerator();
            case "timestamp" -> new TimestampGenerator();
            case "bool" -> new BooleanGenerator();
            case "bytea" -> new ByteaGenerator();
            case "date" -> new DateGenerator();
            // SQL Server integer types
            case "bigint" -> new BigIntGenerator();
            case "smallint" -> new IntegerGenerator(-smallInt, smallInt);
            case "tinyint" -> new IntegerGenerator(0, 255);
            // SQL Server / cross-DB boolean
            case "bit" -> new BooleanGenerator();
            // Unicode and fixed-length strings
            case "nvarchar", "nchar", "ntext" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 50);
            case "char", "bpchar" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 1);
            // SQL Server datetime types
            case "datetime", "datetime2", "smalldatetime" -> new TimestampGenerator();
            // UUID types (PostgreSQL uuid and SQL Server uniqueidentifier)
            case "uuid", "uniqueidentifier" -> new UUIDGenerator();
            // Binary types
            case "varbinary", "image" -> new ByteaGenerator();
            // Float types
            case "float", "real", "double precision", "double" -> new FloatGenerator();
            // Additional money type
            case "smallmoney" -> new MoneyGenerator();
            // PostgreSQL time types
            case "time", "timetz", "time without time zone", "time with time zone" -> new TimeGenerator();
            // JSON types
            case "json", "jsonb" -> new JsonGenerator();
            // DB2 on i type aliases
            case "integer" -> new IntegerGenerator(-normInt, normInt);
            case "character" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 1);
            case "blob" -> new ByteaGenerator();
            case "clob" -> new VarcharGenerator(column.getColumnSize() != null ? column.getColumnSize() : 256);
            // Add other mappings here
            default -> () -> null;
        };
    }
}

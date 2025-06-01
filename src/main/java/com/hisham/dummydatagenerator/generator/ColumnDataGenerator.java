/**
 * Interface for generating dummy data for database columns.
 * Implementations of this interface are responsible for creating appropriate
 * random or sequential values for specific SQL data types.
 *
 * This interface is used by:
 * - DataGeneratorFactory to create type-specific generators
 * - DummyDataService to generate values for table columns
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.generator;

/**
 * Interface for generating column data.
 * Defines the contract for all data generators in the system.
 */
public interface ColumnDataGenerator {
    /**
     * Generates a value appropriate for the column's data type.
     * The generated value should be compatible with the SQL data type
     * and respect any constraints (e.g., size limits, precision).
     *
     * @return Generated value of appropriate type
     */
    Object generate();
}

/**
 * Interface for database schema introspection.
 * Defines the contract for classes that extract metadata about database tables.
 *
 * This interface is used to:
 * - Retrieve table structure information
 * - Get column metadata
 * - Discover constraints and relationships
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.schema;

/**
 * Interface for database schema introspection.
 * Provides methods to extract metadata about database tables and their structure.
 */
public interface DatabaseIntrospector {
    /**
     * Retrieves metadata for a specified table.
     * Returns information about the table's structure, including:
     * - Column names and data types
     * - Primary and foreign keys
     * - Constraints and indexes
     * - Default values and nullability
     *
     * @param schema Database schema name
     * @param tableName Name of the table to introspect
     * @return TableMetadata object containing the table's structure
     */
    TableMetadata getTableMetadata(String schema, String tableName);
}

package com.hisham.dummydatagenerator.datasource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JDBCConnection;
import com.ibm.as400.access.AS400JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory class for creating database connections.
 * Supports multiple database types including DB2 on i (AS/400).
 */
public class DatasourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceProvider.class);

    /**
     * Creates a DataSource for the specified database type and connection details.
     * 
     * @param jdbcUrl The JDBC URL for the database connection
     * @param username The database username
     * @param password The database password
     * @return A configured DataSource instance
     */
    public static DataSource createDataSource(String jdbcUrl, String username, String password) {
        if (jdbcUrl.toLowerCase().contains("as400") || jdbcUrl.toLowerCase().contains("db2i")) {
            return createDB2iDataSource(jdbcUrl, username, password);
        }
        return createStandardDataSource(jdbcUrl, username, password);
    }

    /**
     * Creates a DataSource for DB2 on i (AS/400) systems.
     * 
     * @param jdbcUrl The JDBC URL for the AS/400 connection
     * @param username The AS/400 username
     * @param password The AS/400 password
     * @return A configured DataSource instance for DB2 on i
     */
    private static DataSource createDB2iDataSource(String jdbcUrl, String username, String password) {
        try {
            // Register the JT400 driver
            Class.forName("com.ibm.as400.access.AS400JDBCDriver");
            
            // Create connection properties
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            
            // Additional DB2 on i specific properties
            props.setProperty("libraries", "*LIBL"); // Use library list
            props.setProperty("date format", "iso"); // Use ISO date format
            props.setProperty("time format", "iso"); // Use ISO time format
            
            // Create and return the DataSource
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
            dataSource.setUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setConnectionProperties(props);
            
            // Test the connection
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Successfully connected to DB2 on i system");
            }
            
            return dataSource;
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Failed to create DB2 on i DataSource", e);
            throw new RuntimeException("Failed to create DB2 on i DataSource", e);
        }
    }

    /**
     * Creates a standard DataSource for other database types.
     * 
     * @param jdbcUrl The JDBC URL for the database connection
     * @param username The database username
     * @param password The database password
     * @return A configured DataSource instance
     */
    private static DataSource createStandardDataSource(String jdbcUrl, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}


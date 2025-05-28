package com.hisham.dummydatagenerator;

import com.hisham.dummydatagenerator.controller.UniversalConnectorController;
import com.hisham.dummydatagenerator.connectors.RelationalDatabaseConnector;
import com.hisham.dummydatagenerator.dto.ConnectionRequest;
import com.hisham.dummydatagenerator.dto.ConnectionRequestAll;
import com.hisham.dummydatagenerator.schema.TableMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false",
    "spring.datasource.username=sa",
    "spring.datasource.password=reallyStrongPwd123"
})
public class DummyDataIntegrationTest {

    @Autowired
    private UniversalConnectorController universalConnectorController;

    @Autowired
    private RelationalDatabaseConnector relationalDatabaseConnector;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private static final String TEST_SCHEMA = "TEST_SCHEMA";
    private static final String TEST_TABLE = "TEST_TABLE";
    private static final String TEST_INSERTALL_OUTPUT_STRING = "Insert complete";

    @BeforeEach
    void setUp() {
        
        
        // Create test table using RelationalDatabaseConnector
        String createTableSQL = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '%s')
            BEGIN
                CREATE TABLE %s.%s (
                    id INT PRIMARY KEY,
                    name VARCHAR(100),
                    age INT,
                    created_date DATE,
                    amount DECIMAL(10,2)
                )
            END
            """.formatted(TEST_TABLE, TEST_SCHEMA, TEST_TABLE);
        
        relationalDatabaseConnector.createTable(dataSource, createTableSQL, TEST_TABLE, TEST_SCHEMA);
    }

    @Test
    void testFullDataGenerationFlow() {
        // Truncate test table to ensure it's empty. 
        jdbcTemplate.execute("DELETE FROM %s.%s".formatted(TEST_SCHEMA, TEST_TABLE));
        
        // 1. Create connection request
        ConnectionRequest request = new ConnectionRequest();
        request.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false");
        request.setUsername("sa");
        request.setPassword("reallyStrongPwd123");
        request.setDbType("sqlserver");
        request.setSchema(TEST_SCHEMA);
        request.setTable(TEST_TABLE);

        // 2. Introspect table
        TableMetadata metadata = universalConnectorController.introspect(request);
        assertNotNull(metadata);
        assertEquals(TEST_TABLE, metadata.getTableName());
        assertEquals(5, metadata.getColumns().size());

        // 3. Insert data
        String result = universalConnectorController.insert(10, 1, request);
        assertTrue(result.contains("Inserted 1 transaction(s) with 10 dummy rows"));

        // 4. Verify data
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM %s.%s".formatted(TEST_SCHEMA, TEST_TABLE));
        
        assertEquals(10, rows.size());
        
        // 5. Verify data types
        Map<String, Object> firstRow = rows.get(0);
        assertTrue(firstRow.get("id") instanceof Number, "id should be a Number");
        assertTrue(firstRow.get("name") instanceof String, "name should be a String");
        assertTrue(firstRow.get("age") instanceof Number, "age should be a Number");
        assertTrue(firstRow.get("created_date") instanceof java.sql.Date, "created_date should be a Date");
        assertTrue(firstRow.get("amount") instanceof java.math.BigDecimal, "amount should be a BigDecimal");
    }

    @Test
    void testInsertAllTables() {
        // Truncate test table to ensure it's empty. 
        jdbcTemplate.execute("DELETE FROM %s.%s".formatted(TEST_SCHEMA, TEST_TABLE));

        // Create another test table
        String createTableSQL = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'TEST_TABLE_2')
            BEGIN
                CREATE TABLE %s.TEST_TABLE_2 (
                    id INT PRIMARY KEY,
                    description VARCHAR(200)
                )
            END
            """.formatted(TEST_SCHEMA);

            jdbcTemplate.execute("DELETE FROM %s.%s_2".formatted(TEST_SCHEMA, TEST_TABLE));
        
        relationalDatabaseConnector.createTable(dataSource, createTableSQL, "TEST_TABLE_2", TEST_SCHEMA);

        // Create connection request for all tables
        ConnectionRequestAll request = new ConnectionRequestAll();
        request.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false");
        request.setUsername("sa");
        request.setPassword("reallyStrongPwd123");
        request.setDbType("sqlserver");
        request.setSchema(TEST_SCHEMA);
        request.setRowsPerTable(5);

        // Insert data into all tables
        ResponseEntity<Map<String, Object>> result = universalConnectorController.insertIntoAllTables(request);
        assertEquals(result.getBody().get("message"), TEST_INSERTALL_OUTPUT_STRING);

        // Verify data in both tables
        int count1 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM %s.%s".formatted(TEST_SCHEMA, TEST_TABLE), Integer.class);
        int count2 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM %s.TEST_TABLE_2".formatted(TEST_SCHEMA), Integer.class);
        
        assertEquals(5, count1, "First table should have 5 rows");
        assertEquals(5, count2, "Second table should have 5 rows");
    }
} 
/**
 * Service class for generating and inserting dummy data into database tables.
 * Provides functionality for creating synthetic data based on table metadata and inserting it into the database.
 * Supports concurrent operations for improved performance.
 *
 * This service is responsible for:
 * - Generating rows of dummy data based on column types
 * - Handling primary key uniqueness
 * - Inserting generated data into database tables
 * - Managing database connections and transactions
 * - Concurrent data generation and insertion
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.generator;

import com.hisham.dummydatagenerator.schema.TableMetadata;
import com.hisham.dummydatagenerator.schema.ColumnMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for generating and managing dummy data.
 * Handles the creation and insertion of synthetic data into database tables.
 * Supports concurrent operations for improved performance.
 */
@Service
public class DummyDataService {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${dummy.generator.thread-pool.size:10}")
    private int threadPoolSize;

    private final ThreadPoolTaskExecutor taskExecutor;
    private final ConcurrentHashMap<String, Set<Object>> primaryKeyCache;

    public DummyDataService() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(threadPoolSize);
        this.taskExecutor.setMaxPoolSize(threadPoolSize);
        this.taskExecutor.setQueueCapacity(100);
        this.taskExecutor.setThreadNamePrefix("DataGenerator-");
        this.taskExecutor.initialize();
        this.primaryKeyCache = new ConcurrentHashMap<>();
    }

    /**
     * Generates a specified number of rows of dummy data for a table.
     * Uses concurrent processing for improved performance.
     *
     * @param dataSource DataSource for database connection
     * @param metadata Table metadata containing column information
     * @param rowCount Number of rows to generate
     * @param schema Database schema name
     * @return List of generated rows as maps of column names to values
     */
    public List<Map<String, Object>> generateRows(DataSource dataSource, TableMetadata metadata, int rowCount,
                                                  String schema) {
        logger.info("Generating {} rows for table {}", rowCount, metadata.getTableName());
        logger.debug("Row schema: {}", metadata.getColumns());

        String pkColumn = metadata.getColumns().stream()
                .filter(ColumnMetadata::isPrimaryKey)
                .map(ColumnMetadata::getColumnName)
                .findFirst()
                .orElse(null);

        // Initialize or get the primary key cache for this table
        Set<Object> existingPKs = primaryKeyCache.computeIfAbsent(
            schema + "." + metadata.getTableName(),
            k -> fetchExistingPrimaryKeys(dataSource, schema, metadata.getTableName(), pkColumn)
        );

        // Create a thread-safe list for storing generated rows
        List<Map<String, Object>> rows = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger generated = new AtomicInteger(0);

        // Create a list of futures for concurrent processing
        List<Future<?>> futures = new ArrayList<>();

        // Submit tasks to the thread pool
        while (generated.get() < rowCount) {
            futures.add(taskExecutor.submit(() -> {
                if (generated.get() >= rowCount) return;

                Map<String, Object> row = new HashMap<>();
                for (ColumnMetadata column : metadata.getColumns()) {
                    logger.debug("Generating column {} for row {}", column.getColumnName(), generated.get());
                    ColumnDataGenerator generator = DataGeneratorFactory.getGenerator(column);
                    Object value = generator.generate();
                    logger.debug("Generated value: {}", value);
                    row.put(column.getColumnName(), value);
                }

                if (pkColumn != null) {
                    Object pkValue = row.get(pkColumn);
                    synchronized (existingPKs) {
                        if (existingPKs.contains(pkValue)) return;
                        existingPKs.add(pkValue);
                    }
                }

                rows.add(row);
                generated.incrementAndGet();
            }));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error generating data", e);
                Thread.currentThread().interrupt();
            }
        }

        return rows;
    }

    /**
     * Inserts a list of rows into a specified database table.
     * Uses batch processing and concurrent operations for improved performance.
     *
     * @param schema Database schema name
     * @param tableName Name of the table to insert into
     * @param rows List of rows to insert, each as a map of column names to values
     */
    public void insertRows(String schema, String tableName, List<Map<String, Object>> rows) {
        logger.debug("Inserting rows into table: {}", tableName);

        if (rows.isEmpty()) return;

        String columns = String.join(", ", rows.get(0).keySet());
        String placeholders = String.join(", ", Collections.nCopies(rows.get(0).size(), "?"));
        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholders);

        // Use batch processing for better performance
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> row = rows.get(i);
                int paramIndex = 1;
                for (Object value : row.values()) {
                    ps.setObject(paramIndex++, value);
                }
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    /**
     * Fetches existing primary key values from a table.
     * Uses thread-safe operations for concurrent access.
     *
     * @param dataSource DataSource for database connection
     * @param schema Database schema name
     * @param tableName Name of the table to query
     * @param primaryKeyColumn Name of the primary key column
     * @return Set of existing primary key values
     * @throws RuntimeException if there is an error accessing the database
     */
    public Set<Object> fetchExistingPrimaryKeys(DataSource dataSource, String schema, String tableName, String primaryKeyColumn) {
        Set<Object> primaryKeys = ConcurrentHashMap.newKeySet();
        String sql = String.format("SELECT %s FROM %s.%s", primaryKeyColumn, schema, tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                primaryKeys.add(rs.getObject(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching primary keys: ", e);
        }

        return primaryKeys;
    }
}

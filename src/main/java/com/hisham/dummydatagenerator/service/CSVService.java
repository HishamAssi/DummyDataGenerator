package com.hisham.dummydatagenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Service for handling CSV file operations.
 * Provides functionality to write data to CSV files with optional headers.
 */
@Service
public class CSVService {
    private static final Logger logger = LoggerFactory.getLogger(CSVService.class);

    @Value("${dummy.generator.csv.output.dir:./output}")
    private String outputDir;

    @Value("${dummy.generator.csv.include.header:true}")
    private boolean includeHeader;

    /**
     * Writes data to a CSV file.
     *
     * @param schema Database schema name
     * @param tableName Name of the table
     * @param rows List of rows to write
     * @return Path to the created CSV file
     * @throws IOException if there is an error writing to the file
     */
    public Path writeToCSV(String schema, String tableName, List<Map<String, Object>> rows) throws IOException {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("No rows to write");
        }

        // Create output directory if it doesn't exist
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Create filename with timestamp
        String filename = String.format("%s_%s_%d.csv", schema, tableName, System.currentTimeMillis());
        Path filePath = outputPath.resolve(filename);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Write header if enabled
            if (includeHeader) {
                String header = String.join(",", rows.get(0).keySet());
                writer.write(header + "\n");
            }

            // Write data rows
            for (Map<String, Object> row : rows) {
                String line = row.values().stream()
                        .map(value -> value == null ? "" : value.toString())
                        .map(value -> value.contains(",") ? "\"" + value + "\"" : value)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
                writer.write(line + "\n");
            }
        }

        logger.info("Wrote {} rows to CSV file: {}", rows.size(), filePath);
        return filePath;
    }
} 
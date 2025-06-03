package com.hisham.dummydatagenerator.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Kafka producer settings.
 * Provides methods to convert configuration to a map for Kafka producer creation.
 */
public class KafkaProducerConfig {
    private String bootstrapServers;
    private String acks;
    private Integer retries;
    private Integer batchSize;
    private Integer lingerMs;
    private Integer bufferMemory;

    public KafkaProducerConfig() {
        // Default values
        this.bootstrapServers = "localhost:9092";
        this.acks = "all";
        this.retries = 3;
        this.batchSize = 16384;
        this.lingerMs = 1;
        this.bufferMemory = 33554432;
    }

    /**
     * Converts the configuration to a map of Kafka producer properties.
     *
     * @return Map of Kafka producer configuration properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("acks", acks);
        config.put("retries", retries);
        config.put("batch.size", batchSize);
        config.put("linger.ms", lingerMs);
        config.put("buffer.memory", bufferMemory);
        return config;
    }

    // Getters and setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(Integer lingerMs) {
        this.lingerMs = lingerMs;
    }

    public Integer getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(Integer bufferMemory) {
        this.bufferMemory = bufferMemory;
    }
} 
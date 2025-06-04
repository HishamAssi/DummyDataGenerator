/**
 * Service class for handling Kafka operations in the application.
 * Manages the production and sending of messages to Kafka topics.
 *
 * This service is responsible for:
 * - Initializing Kafka configuration
 * - Creating and managing KafkaTemplate instances
 * - Sending table data to Kafka topics
 * - Handling message serialization
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.service;

import com.hisham.dummydatagenerator.config.KafkaConfig;
import com.hisham.dummydatagenerator.config.KafkaProducerConfig;
import com.hisham.dummydatagenerator.dto.TableDataMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Service for sending data to Kafka topics.
 * Supports concurrent operations for improved performance.
 */
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
    private final KafkaConfig kafkaConfig;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ConcurrentHashMap<String, KafkaProducer<String, Object>> producerCache;

    @Value("${kafka.producer.thread-pool.size:5}")
    private int threadPoolSize;

    /**
     * Constructs a new KafkaService instance.
     * Initializes the Kafka configuration.
     */
    public KafkaService() {
        this.kafkaConfig = new KafkaConfig();
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(threadPoolSize);
        this.taskExecutor.setMaxPoolSize(threadPoolSize);
        this.taskExecutor.setQueueCapacity(100);
        this.taskExecutor.setThreadNamePrefix("KafkaProducer-");
        this.taskExecutor.initialize();
        this.producerCache = new ConcurrentHashMap<>();
    }

    /**
     * Sends table data to a Kafka topic.
     * Uses concurrent processing for improved performance.
     *
     * @param topic Kafka topic to send data to
     * @param tableName Name of the table being sent
     * @param schema Database schema name
     * @param rows List of rows to send
     * @param kafkaConfig Additional Kafka configuration
     */
    public void sendTableData(String topic, String tableName, String schema, List<Map<String, Object>> rows,
                            Map<String, Object> kafkaConfig) {
        logger.info("Sending {} rows to Kafka topic: {}", rows.size(), topic);

        // Get or create a Kafka producer for this topic
        KafkaProducer<String, Object> producer = producerCache.computeIfAbsent(topic,
            k -> this.kafkaConfig.createKafkaProducer(kafkaConfig));

        // Create a list of futures for concurrent processing
        List<Future<RecordMetadata>> futures = new ArrayList<>();
        AtomicInteger sent = new AtomicInteger(0);

        // Submit tasks to the thread pool
        for (Map<String, Object> row : rows) {
            futures.add(taskExecutor.submit(() -> {
                String key = schema + "." + tableName + "." + sent.getAndIncrement();
                ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, row);
                return producer.send(record).get();
            }));
        }

        // Wait for all tasks to complete
        for (Future<RecordMetadata> future : futures) {
            try {
                RecordMetadata metadata = future.get();
                logger.debug("Sent record to topic {} partition {} offset {}", 
                    metadata.topic(), metadata.partition(), metadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error sending data to Kafka", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Closes all Kafka producers and releases resources.
     * Should be called when the service is no longer needed.
     */
    public void close() {
        producerCache.values().forEach(KafkaProducer::close);
        taskExecutor.shutdown();
    }
}
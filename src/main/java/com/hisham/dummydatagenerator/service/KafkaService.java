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
import com.hisham.dummydatagenerator.dto.KafkaProducerConfig;
import com.hisham.dummydatagenerator.dto.TableDataMessage;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Service for Kafka operations.
 * Handles the production and sending of messages to Kafka topics.
 */
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaConfig kafkaConfig;
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Constructs a new KafkaService instance.
     * Initializes the Kafka configuration.
     */
    public KafkaService() {
        this.kafkaConfig = new KafkaConfig();
    }

    /**
     * Sends table data to a specified Kafka topic.
     * Creates a KafkaTemplate if not already initialized and sends each row as a message.
     *
     * @param topic The Kafka topic to send messages to
     * @param tableName The name of the table whose data is being sent
     * @param schema The database schema name
     * @param rows List of row data to be sent
     * @param producerConfig Configuration for the Kafka producer
     */
    public void sendTableData(String topic, String tableName, String schema, List<Map<String, Object>> rows, KafkaProducerConfig producerConfig) {
        if (kafkaTemplate == null) {
            kafkaTemplate = kafkaConfig.createKafkaTemplate(producerConfig);
        }

        for (Map<String, Object> row : rows) {
            TableDataMessage message = new TableDataMessage(tableName, schema, row);
            logger.info("Sending data to Kafka for table {}.{}", schema, tableName);
            kafkaTemplate.send(topic, tableName, message);
        }
    }
}
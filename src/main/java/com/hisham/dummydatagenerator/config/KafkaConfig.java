/**
 * Configuration class for Kafka producer settings.
 * Provides methods to create and configure Kafka producer factories and templates.
 *
 * This class is responsible for:
 * - Configuring Kafka producer properties
 * - Creating producer factories
 * - Initializing Kafka templates
 * - Managing serialization settings
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.config;

import com.hisham.dummydatagenerator.dto.KafkaProducerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Kafka configuration and template creation.
 * Handles the setup of Kafka producers and their properties.
 */
public class KafkaConfig {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    /** Default Kafka bootstrap servers address */
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

    /**
     * Creates a Kafka producer factory with the specified configuration.
     * Configures serializers, bootstrap servers, and additional properties.
     *
     * @param config Kafka producer configuration containing bootstrap servers, serializers, and additional properties
     * @return Configured ProducerFactory instance
     */
    public ProducerFactory<String, Object> createProducerFactory(KafkaProducerConfig config) {
        logger.trace("Creating producer factory with bootstrap servers: {}", 
            config.getBootstrapServers() != null ? config.getBootstrapServers() : DEFAULT_BOOTSTRAP_SERVERS);
        
        Map<String, Object> configProps = new HashMap<>();
        
        // Set bootstrap servers
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getBootstrapServers() != null ? config.getBootstrapServers() : DEFAULT_BOOTSTRAP_SERVERS);
        
        // Set serializers
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
            config.getKeySerializer() != null ? config.getKeySerializer() : StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
            config.getValueSerializer() != null ? config.getValueSerializer() : JsonSerializer.class);
        
        // Add any additional properties
        if (config.getAdditionalProperties() != null) {
            configProps.putAll(config.getAdditionalProperties());
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate instance using the provided configuration.
     * The template is configured with the producer factory created from the config.
     *
     * @param config Kafka producer configuration
     * @return Configured KafkaTemplate instance
     */
    public KafkaTemplate<String, Object> createKafkaTemplate(KafkaProducerConfig config) {
        logger.trace("Creating KafkaTemplate");
        return new KafkaTemplate<>(createProducerFactory(config));
    }
} 
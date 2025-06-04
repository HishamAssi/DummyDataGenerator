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

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;

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

    /**
     * Creates a Kafka producer with the specified configuration.
     *
     * @param config Additional configuration for the producer
     * @return Configured KafkaProducer instance
     */
    public KafkaProducer<String, Object> createKafkaProducer(Map<String, Object> config) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getOrDefault("bootstrap.servers", DEFAULT_BOOTSTRAP_SERVERS));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // Add any additional configuration
        props.putAll(config);

        logger.info("Creating Kafka producer with bootstrap servers: {}", 
            props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        return new KafkaProducer<>(props);
    }
} 
package com.hisham.dummydatagenerator.config;

import com.hisham.dummydatagenerator.dto.KafkaProducerConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String defaultBootstrapServers;

    @Bean
    public NewTopic tableDataTopic() {
        return new NewTopic("table-data", 1, (short) 1);
    }

    public ProducerFactory<String, Object> createProducerFactory(KafkaProducerConfig config) {
        Map<String, Object> configProps = new HashMap<>();
        
        // Set bootstrap servers
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getBootstrapServers() != null ? config.getBootstrapServers() : defaultBootstrapServers);
        
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

    public KafkaTemplate<String, Object> createKafkaTemplate(KafkaProducerConfig config) {
        return new KafkaTemplate<>(createProducerFactory(config));
    }

    // Default producer factory for backward compatibility
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        KafkaProducerConfig defaultConfig = new KafkaProducerConfig();
        defaultConfig.setBootstrapServers(defaultBootstrapServers);
        return createProducerFactory(defaultConfig);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
} 
package com.hisham.dummydatagenerator.service;

import com.hisham.dummydatagenerator.config.KafkaConfig;
import com.hisham.dummydatagenerator.dto.KafkaProducerConfig;
import com.hisham.dummydatagenerator.dto.TableDataMessage;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaTemplate<String, Object> defaultKafkaTemplate;

    public void sendTableData(String topic, String tableName, String schema, List<Map<String, Object>> rows, KafkaProducerConfig producerConfig) {
        KafkaTemplate<String, Object> kafkaTemplate = producerConfig != null ? 
            kafkaConfig.createKafkaTemplate(producerConfig) : defaultKafkaTemplate;

        for (Map<String, Object> row : rows) {
            TableDataMessage message = new TableDataMessage(tableName, schema, row);
            logger.info("Sending data to Kafka for table {}.{}", schema, tableName);
            kafkaTemplate.send(topic, tableName, message);
        }
    }
}
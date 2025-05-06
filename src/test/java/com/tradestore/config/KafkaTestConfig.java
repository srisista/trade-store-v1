package com.tradestore.config;

import com.tradestore.domain.model.Trade;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@TestConfiguration
@EmbeddedKafka(partitions = 1, topics = {"trades"})
public class KafkaTestConfig {

    @Bean
    public ProducerFactory<String, Trade> producerFactory(EmbeddedKafkaBroker broker) {
        Map<String, Object> configProps = KafkaTestUtils.producerProps(broker);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Trade> kafkaTemplate(ProducerFactory<String, Trade> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
} 
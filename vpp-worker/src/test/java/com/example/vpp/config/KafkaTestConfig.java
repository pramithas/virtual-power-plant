package com.example.vpp.config;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;

@TestConfiguration
public class KafkaTestConfig {

    @Bean
    @Primary
    public Producer<String, Object> kafkaProducer() {
        return Mockito.mock(Producer.class);
    }
}

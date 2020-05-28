package com.task.manager.configuration;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Bean
    @RefreshScope
    public KafkaSender<String, String> kafkaSender(@Value("kafka") Map<String, Object> properties) {
        var consumerOptions = SenderOptions.<String, String>create(properties)
            .withValueSerializer(new StringSerializer())
            .withKeySerializer(new StringSerializer());
        return KafkaSender.create(consumerOptions);
    }
}

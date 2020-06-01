package com.task.provider.configuration;

import lombok.Setter;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
public class KafkaConfiguration {

    @Bean
    @ConfigurationProperties("kafka")
    KafkaConsumerProperties kafkaProperties() {
        return new KafkaConsumerProperties();
    }

    @Bean
    @RefreshScope
    public KafkaSender<String, String> kafkaSender(KafkaConsumerProperties kafkaProperties) {
        var consumerOptions = SenderOptions.<String, String>create(kafkaProperties.properties)
                .withValueSerializer(new StringSerializer())
                .withKeySerializer(new StringSerializer());
        return KafkaSender.create(consumerOptions);
    }

    @Setter
    public static class KafkaConsumerProperties {

        public Map<String, Object> properties = new HashMap<>();
    }
}

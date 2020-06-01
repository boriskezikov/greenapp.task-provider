package com.task.provider.service.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

import static com.task.provider.exception.InvocationError.KAFKA_INVOCATION_ERROR;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class KafkaAdapter {

    private final static Logger log = LoggerFactory.getLogger(KafkaAdapter.class);

    private final KafkaProducer producer;

    public Flux<Void> sendEvent(Event event) {
        var record = senderRecord(event);
        return producer.send(record)
                .flatMap(r -> isNull(r.exception()) ?
                        Mono.empty().then() :
                        Mono.error(r.exception())
                )
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100)))
                .doOnSubscribe(i -> log.info("KafkaAdapter.sendEvent.in event = {}", event))
                .doOnComplete(() -> log.info("KafkaAdapter.sendEvent.out"))
                .onErrorMap(KAFKA_INVOCATION_ERROR::exception);
    }

    private Mono<SenderRecord<String, String, Void>> senderRecord(Event event) {
        var record = new ProducerRecord<String, String>(event.topic, event.value);
        return Mono.just(SenderRecord.create(record, null));
    }

    @RequiredArgsConstructor
    public static class Event {

        public final String topic;
        public final String value;
    }
}


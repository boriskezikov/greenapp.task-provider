package com.task.provider.logic;

import com.task.provider.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.provider.model.Status;
import com.task.provider.service.dao.R2dbcAdapter;
import com.task.provider.service.kafka.KafkaAdapter;
import com.task.provider.service.kafka.KafkaAdapter.Event;
import io.r2dbc.client.Update;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.task.provider.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class UpdateStatusOperation {

    private final static Logger log = LoggerFactory.getLogger(UpdateStatusOperation.class);

    public final R2dbcAdapter r2dbcAdapter;
    private final KafkaAdapter kafkaAdapter;

    public Mono<Void> process(UpdateStatusRequest request) {
        var oldTask = this.r2dbcAdapter.findById(new FindTaskByIdRequest(request.taskId))
            .map(t -> t.status)
            .switchIfEmpty(Mono.error(new RuntimeException("No such task exist with id = " + request.taskId)));
        return request.asMono()
            .map(r -> r.status)
            .zipWith(oldTask)
            .map(t -> {
                t.getT1().validateOver(t.getT2());
                return request;
            })
            .flatMap(r2dbcAdapter::updateStatus)
            .then(kafkaAdapter.sendEvent(new Event("StatusChanged", request.taskId, null, request.status)))
            .as(logProcess(log, "UpdateStatusOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class UpdateStatusRequest {

        public final Long taskId;
        public final Status status;

        public Mono<UpdateStatusRequest> asMono() {
            return Mono.just(this);
        }

        public Update bindOn(Update query) {
            return query
                .bind("$1", this.status.toString())
                .bind("$2", this.taskId);
        }
    }
}

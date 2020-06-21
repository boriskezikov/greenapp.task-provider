package com.task.provider.logic;

import com.task.provider.model.Binder;
import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import com.task.provider.service.dao.R2dbcHandler;
import com.task.provider.service.kafka.KafkaAdapter;
import com.task.provider.service.kafka.KafkaAdapter.Event;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.task.provider.exception.ValidationError.INVALID_ATTACH_REQUEST;
import static com.task.provider.utils.Utils.logProcess;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class CreateTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(CreateTaskOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;
    private final KafkaAdapter kafkaAdapter;

    public Mono<Long> process(CreateTaskRequest request) {
        return r2dbcHandler.inTxMono(
            h -> {
                var taskIdMono = r2dbcAdapter.insert(h, request).cache();
                var attachPhotosMono = taskIdMono
                    .flatMapMany(id -> Flux.fromIterable(request.attachPhotosRequest)
                        .map(a -> {
                            a.setTaskId(id);
                            return a;
                        })
                        .flatMap(AttachPhotosRequest::validate)
                        .flatMap(r -> r2dbcAdapter.attach(h, r))
                    );
                var sendEventMono = taskIdMono
                    .map(id -> new Event("TaskCreated", id, request.newTask.createdBy))
                    .flatMapMany(kafkaAdapter::sendEvent);
                return Mono.when(attachPhotosMono)
                    .then(taskIdMono);
            }
        ).as(logProcess(log, "CreateTaskOperation", request));
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class CreateTaskRequest extends Binder {

        public final Task newTask;
        public final List<AttachPhotosRequest> attachPhotosRequest;

        public Query bindOn(Query query) {
            bind(query, "$1", String.class, newTask.title);
            bind(query, "$2", String.class, newTask.description);
            bind(query, "$3", String.class, newTask.type.toString());
            bind(query, "$4", Long.class, newTask.reward);
            bind(query, "$5", LocalDateTime.class, newTask.dueDate);
            bind(query, "$6", String.class, newTask.coordinate.toString());
            bind(query, "$7", Long.class, newTask.createdBy);
            return query;
        }
    }

    @Setter
    @ToString
    @RequiredArgsConstructor
    public static class AttachPhotosRequest extends Binder {

        private Long taskId;
        public final String contentType;
        public final Long contentLength;
        public final byte[] content;

        public Query bindOn(Query query) {
            query
                .bind("$1", this.taskId)
                .bind("$4", this.content);
            bind(query, "$2", String.class, this.contentType);
            bind(query, "$3", Long.class, this.contentLength);
            return query;
        }

        public Mono<AttachPhotosRequest> asMono() {
            return Mono.just(this);
        }

        public Mono<AttachPhotosRequest> validate() {
            if (isNull(taskId)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Task id cannot be null");
            } else if (isNull(contentType)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Content-Type cannot be null");
            } else if (isNull(content)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Content cannot be null");
            }
            return asMono();
        }
    }
}

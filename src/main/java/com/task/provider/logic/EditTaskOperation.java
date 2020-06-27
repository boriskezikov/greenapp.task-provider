package com.task.provider.logic;

import com.task.provider.logic.CreateTaskOperation.AttachPhotosRequest;
import com.task.provider.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.provider.model.Binder;
import com.task.provider.model.Status;
import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import com.task.provider.service.dao.R2dbcHandler;
import com.task.provider.service.kafka.KafkaAdapter;
import com.task.provider.service.kafka.KafkaAdapter.Event;
import io.r2dbc.client.Update;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.task.provider.exception.ApplicationError.TASK_NOT_FOUND_BY_ID;
import static com.task.provider.utils.Utils.logProcess;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class EditTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(EditTaskOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;
    private final KafkaAdapter kafkaAdapter;

    public Mono<Void> process(EditTaskRequest request) {
        var id = request.newTask.id;
        var oldTask = r2dbcAdapter.findById(new FindTaskByIdRequest(id))
            .switchIfEmpty(TASK_NOT_FOUND_BY_ID.exceptionMono("No such task exist with id = " + id));
        return request.asMono()
            .zipWith(oldTask)
            .flatMap(t -> t.getT1().updateRequest(t.getT2()))
            .flatMap(r -> r2dbcHandler.inTxMono(h -> {
                var updateTask = r2dbcAdapter.update(h, r);
                var detach = request.detach ?
                             r2dbcAdapter.detach(h, id) :
                             Mono.empty();
                var attach = Flux.fromIterable(request.attachPhotosRequest)
                    .flatMap(a -> r2dbcAdapter.attach(h, a));
                var sendEvent = kafkaAdapter.sendEvent(new Event("TaskEdited", id, request.newTask.createdBy, r.newTask.status));
                return Mono.when(updateTask, detach, attach, sendEvent);
            }))
            .as(logProcess(log, "EditTaskOperation", request));
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class EditTaskRequest {

        public final Task newTask;
        public final List<AttachPhotosRequest> attachPhotosRequest;
        public final boolean detach;

        public Mono<EditTaskRequest> asMono() {
            return Mono.just(this);
        }

        public Mono<UpdateTaskRequest> updateRequest(Task oldTask) {
            if (oldTask.equals(this.newTask)) {
                return Mono.empty();
            }
            if (isNull(oldTask.assignee) && nonNull(newTask.assignee)) {
                return new UpdateTaskRequest(Task.withNewStatus(newTask, Status.IN_PROGRESS)).asMono();
            } else if (nonNull(oldTask.assignee) && isNull(newTask.assignee)) {
                return new UpdateTaskRequest(Task.withNewStatus(newTask, Status.TO_DO)).asMono();
            }
            return new UpdateTaskRequest(newTask).asMono();
        }
    }

    @ToString
    @RequiredArgsConstructor
    public static class UpdateTaskRequest extends Binder {

        public final Task newTask;

        public Mono<UpdateTaskRequest> asMono() {
            return Mono.just(this);
        }

        public Update bindOn(Update query) {
            bind(query, "$1", String.class, newTask.title);
            bind(query, "$2", String.class, newTask.description);
            bind(query, "$3", String.class, newTask.type.toString());
            bind(query, "$4", Long.class, newTask.reward);
            bind(query, "$5", LocalDateTime.class, newTask.dueDate);
            bind(query, "$6", String.class, newTask.coordinate.toString());
            bind(query, "$7", Long.class, newTask.id);
            bind(query, "$8", Long.class, newTask.assignee);
            return query;
        }
    }
}

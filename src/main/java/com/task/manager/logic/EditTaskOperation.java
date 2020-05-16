package com.task.manager.logic;

import com.task.manager.model.Binder;
import com.task.manager.model.Status;
import com.task.manager.model.Task;
import com.task.manager.model.Type;
import com.task.manager.service.R2dbcAdapter;
import io.r2dbc.client.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EditTaskOperation {

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Void> process(EditTaskRequest request) {
        var oldTask = r2dbcAdapter.findById(request.taskId)
            .switchIfEmpty(Mono.error(new RuntimeException("No such task exist with id = " + request.taskId)));
        return request.asMono()
            .map(r -> r.newTask)
            .zipWith(oldTask)
            .flatMap(t -> t.getT2().updateRequest(t.getT1()))
            .flatMap(r2dbcAdapter::update)
            .then();
    }

    @RequiredArgsConstructor
    public static class EditTaskRequest {

        public final Task newTask;
        public final Long taskId;

        public Mono<EditTaskRequest> asMono() {
            return Mono.just(this);
        }
    }

    public static class UpdateTaskRequest extends Binder {

        public final Long id;
        public final String title;
        public final String description;
        public final Status status;
        public final Task.Point coordinate;
        public final Type type;
        //public final List<Byte[]> photo;
        public final Long reward;
        public final Long assignee;
        public final LocalDateTime dueDate;

        public UpdateTaskRequest(Task newTask) {
            this.id = newTask.id;
            this.title = newTask.title;
            this.description = newTask.description;
            this.status = newTask.status;
            this.coordinate = newTask.coordinate;
            this.type = newTask.type;
            this.reward = newTask.reward;
            this.assignee = newTask.assignee;
            this.dueDate = newTask.dueDate;
        }

        public Mono<UpdateTaskRequest> asMono() {
            return Mono.just(this);
        }

        public Update bindOn(Update query) {
            return query
                .bind("$1", this.title)
                .bind("$2", this.description)
                .bind("$3", this.status)
                .bind("$4", this.coordinate.toString())
                .bind("$5", this.type)
                .bind("$6", this.reward)
                .bind("$7", this.dueDate)
                .bind("$8", this.id);
        }
    }


}

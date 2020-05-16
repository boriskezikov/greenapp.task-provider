package com.task.manager.logic;

import com.task.manager.model.Binder;
import com.task.manager.model.Task;
import com.task.manager.service.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateTaskOperation {

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Long> process(CreateTaskRequest request) {
        return r2dbcAdapter.insert(request);
    }

    @RequiredArgsConstructor
    public static class CreateTaskRequest extends Binder {

        public final Task newTask;

        public Query bindOn(Query query) {
            bind(query, "$1", String.class, newTask.title);
            bind(query, "$2", String.class, newTask.description);
            bind(query, "$3", String.class, newTask.coordinate.toString());
            bind(query, "$4", String.class, newTask.type.toString());
            bind(query, "$5", Long.class, newTask.reward);
            bind(query, "$6", LocalDateTime.class, newTask.dueDate);
            bind(query, "$7", Long.class, newTask.createdBy);
            return query;
        }
    }
}

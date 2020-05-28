package com.task.provider.logic;

import com.task.provider.model.Binder;
import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
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
            bind(query, "$3", String.class, newTask.type.toString());
            bind(query, "$4", Long.class, newTask.reward);
            bind(query, "$5", LocalDateTime.class, newTask.dueDate);
            bind(query, "$6", String.class, newTask.coordinate.toString());
            bind(query, "$7", Long.class, newTask.createdBy);
            return query;
        }
    }
}

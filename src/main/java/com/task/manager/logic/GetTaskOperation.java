package com.task.manager.logic;

import com.task.manager.model.Task;
import com.task.manager.service.R2dbcAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GetTaskOperation {

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Task> process(GetTaskRequest request) {
        return r2dbcAdapter.findById(request.taskId)
            .switchIfEmpty(Mono.error(new RuntimeException("No such task exist with id = " + request.taskId)));
    }

    @RequiredArgsConstructor
    public static class GetTaskRequest {

        public final Long taskId;
    }
}

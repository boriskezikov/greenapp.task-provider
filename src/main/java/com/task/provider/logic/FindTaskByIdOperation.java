package com.task.provider.logic;

import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.task.provider.exception.ApplicationError.TASK_NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
public class FindTaskByIdOperation {

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Task> process(FindTaskByIdRequest request) {
        return r2dbcAdapter.findById(request)
                .switchIfEmpty(TASK_NOT_FOUND_BY_ID.exceptionMono("No such task exist with id = " + request.taskId));
    }

    @RequiredArgsConstructor
    public static class FindTaskByIdRequest {

        public final Long taskId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.taskId);
        }
    }
}

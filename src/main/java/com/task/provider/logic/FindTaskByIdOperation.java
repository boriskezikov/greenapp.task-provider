package com.task.provider.logic;

import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.task.provider.exception.ApplicationError.TASK_NOT_FOUND_BY_ID;
import static com.task.provider.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class FindTaskByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindTaskByIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Task> process(FindTaskByIdRequest request) {
        return r2dbcAdapter.findById(request)
            .switchIfEmpty(TASK_NOT_FOUND_BY_ID.exceptionMono("No such task exist with id = " + request.taskId))
            .as(logProcess(log, "FindTaskByIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class FindTaskByIdRequest {

        public final Long taskId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.taskId);
        }
    }
}

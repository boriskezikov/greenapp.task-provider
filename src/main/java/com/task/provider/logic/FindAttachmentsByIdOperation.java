package com.task.provider.logic;

import com.task.provider.model.Task.Attachment;
import com.task.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.task.provider.Utils.logProcessFlux;
import static com.task.provider.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class FindAttachmentsByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentsByIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Attachment> process(FindAttachmentsByTaskIdRequest request) {
        return r2dbcAdapter.findAttachmentsByTaskId(request)
            .switchIfEmpty(
                ATTACHMENTS_NOT_FOUND.exceptionMono(
                    "No attachments found for task with id = " + request.taskId))
            .as(logProcessFlux(log, "FindAttachmentsByIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class FindAttachmentsByTaskIdRequest {

        public final Long taskId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.taskId);
        }
    }
}

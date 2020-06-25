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

import static com.task.provider.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;
import static com.task.provider.utils.Utils.logProcessFlux;

@Component
@RequiredArgsConstructor
public class FindAttachmentsByTaskIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentsByTaskIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Attachment> process(FindAttachmentsByTaskIdRequest request) {
        return r2dbcAdapter.findAttachmentsByTaskId(request)
            .switchIfEmpty(
                ATTACHMENTS_NOT_FOUND.exceptionMono(
                    "No attachments found for task with id = " + request.taskId))
            .as(logProcessFlux(log, "FindAttachmentsByTaskIdOperation", request));
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

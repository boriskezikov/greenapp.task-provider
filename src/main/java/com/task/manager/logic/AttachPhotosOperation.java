package com.task.manager.logic;

import com.task.manager.service.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.task.manager.exception.ValidationError.INVALID_ATTACH_REQUEST;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AttachPhotosOperation {

    public final R2dbcAdapter r2dbcAdapter;

    public Mono<Void> process(AttachPhotosRequest request) {
        return request.asMono()
                .flatMap(AttachPhotosRequest::validate)
                .flatMap(r2dbcAdapter::attach)
                .then();
    }

    @RequiredArgsConstructor
    public static class AttachPhotosRequest {

        public final Long taskId;
        public final String contentType;
        public final Long contentLength;
        public final byte[] content;

        public Query bindOn(Query query) {
            return query
                    .bind("$1", this.taskId)
                    .bind("$2", this.contentType)
                    .bind("$3", this.contentLength)
                    .bind("$4", this.content);
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

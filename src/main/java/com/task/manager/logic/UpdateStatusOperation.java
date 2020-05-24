package com.task.manager.logic;

import com.task.manager.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.manager.model.Status;
import com.task.manager.service.R2dbcAdapter;
import io.r2dbc.client.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UpdateStatusOperation {

    public final R2dbcAdapter r2dbcAdapter;

    public Mono<Void> process(UpdateStatusRequest request) {
        var oldTask = this.r2dbcAdapter.findById(new FindTaskByIdRequest(request.taskId))
            .map(t -> t.status)
            .switchIfEmpty(Mono.error(new RuntimeException("No such task exist with id = " + request.taskId)));
        return request.asMono()
            .map(r -> r.status)
            .zipWith(oldTask)
            .map(t -> {
                t.getT1().validateOver(t.getT2());
                return request;
            })
            .flatMap(r2dbcAdapter::updateStatus)
            .then();
    }

    @RequiredArgsConstructor
    public static class UpdateStatusRequest {

        public final Long taskId;
        public final Status status;

        public Mono<UpdateStatusRequest> asMono() {
            return Mono.just(this);
        }

        public Update bindOn(Update query) {
            return query
                .bind("$1", this.status)
                .bind("$2", this.taskId);
        }
    }
}

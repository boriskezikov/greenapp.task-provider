package com.task.manager.service;

import com.task.manager.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.manager.logic.EditTaskOperation.UpdateTaskRequest;
import com.task.manager.logic.FindTasksOperation.FindTasksRequest;
import com.task.manager.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.manager.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class R2dbcAdapter {

    private final R2dbcHandler handler;

    public Mono<Task> findById(long id) {
        return this.handler.withHandle(h -> {
            var sql = "SELECT id, title, description, CAST(status AS VARCHAR), CAST(type AS VARCHAR), CAST(coordinate AS VARCHAR), " +
                "reward, assignee, due_date, updated_by, updated, created_by, created " +
                "FROM public.task WHERE id = $1";
            return h.createQuery(sql)
                .bind("$1", id)
                .mapRow(Task::fromGetByIdRow)
                .next();
        });
    }

    public Flux<Task> findList(FindTasksRequest request) {
        return this.handler.withHandleFlux(h -> {
            var sql = request.appendSqlOver(
                "SELECT id, title, CAST(status AS VARCHAR), CAST(type AS VARCHAR), CAST(coordinate AS VARCHAR), reward, due_date " +
                    "FROM public.task"
            );
            return request.bindOn(h.createQuery(sql))
                .mapRow(Task::fromFindRow);
        });
    }

    public Mono<Long> insert(CreateTaskRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "INSERT INTO public.task (title, description, status, type, reward, due_date, coordinate, created_by) " +
                "VALUES($1, $2, 'CREATED', $4, $5, $6, point($7), $8) RETURNING id";
            return request.bindOn(h.createQuery(sql))
                .mapRow(r -> r.get("id", Long.class))
                .next();
        });
    }

    public Mono<Integer> updateStatus(UpdateStatusRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "UPDATE public.task SET status = $1 WHERE id = $2";
            return request.bindOn(h.createUpdate(sql))
                .execute()
                .next();
        });
    }

    public Mono<Integer> update(UpdateTaskRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "UPDATE public.task SET title = $1, description = $2, status = $3, coordinate = $4, type = $5, reward = $6, " +
                "due_date = $7, updated = now()" +
                "WHERE id = $8";
            return request.bindOn(h.createUpdate(sql))
                .execute()
                .next();
        });
    }
}

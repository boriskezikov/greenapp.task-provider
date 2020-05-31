package com.task.provider.service.dao;

import com.task.provider.logic.CreateTaskOperation.AttachPhotosRequest;
import com.task.provider.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.provider.logic.EditTaskOperation.UpdateTaskRequest;
import com.task.provider.logic.FindAttachmentsByIdOperation.FindAttachmentsByTaskIdRequest;
import com.task.provider.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.provider.logic.FindTasksOperation.FindTasksRequest;
import com.task.provider.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.provider.model.Task;
import com.task.provider.model.Task.Attachment;
import io.r2dbc.client.Handle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class R2dbcAdapter {

    private final R2dbcHandler handler;

    public Mono<Task> findById(FindTaskByIdRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "SELECT id, title, description, CAST(status AS VARCHAR), CAST(type AS VARCHAR), CAST(coordinate AS VARCHAR), " +
                "reward, assignee, due_date, updated, created_by, created " +
                "FROM public.task WHERE id = $1";
            return request.bindOn(h.createQuery(sql))
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

    public Flux<Attachment> findAttachmentsByTaskId(FindAttachmentsByTaskIdRequest request) {
        return this.handler.withHandleFlux(h -> {
            var sql = "SELECT id, task_id, content, type, length FROM public.attachment WHERE task_id = $1";
            return request.bindOn(h.createQuery(sql))
                .mapRow(Attachment::fromRow);
        });
    }

    public Mono<Long> insert(@Nullable Handle handle, CreateTaskRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> insert(h, request));
        }
        var sql = "INSERT INTO public.task (title, description, status, type, reward, due_date, coordinate, created_by) " +
            "VALUES($1, $2, 'CREATED', $3::task_type, $4, $5, point($6), $7) RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> attach(@Nullable Handle handle, AttachPhotosRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> attach(h, request));
        }
        var sql = "INSERT INTO public.attachment (task_id, type, length, content) " +
            "VALUES($1, $2, $3, $4) RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> detach(@Nullable Handle handle, long taskId) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> detach(h, taskId));
        }
        var sql = "DELETE FROM public.attachment WHERE task_id = $1 RETURNING id";
        return handle.createQuery(sql)
            .bind("$1", taskId)
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Integer> updateStatus(UpdateStatusRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "UPDATE public.task SET status = $1::task_status WHERE id = $2";
            return request.bindOn(h.createUpdate(sql))
                .execute()
                .next();
        });
    }

    public Mono<Integer> update(@Nullable Handle handle, UpdateTaskRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> update(h, request));
        }
        var sql = "UPDATE public.task SET title = $1, description = $2, type = $3::task_type, reward = $4, " +
            "due_date = $5, coordinate = point($6), updated = now() " +
            "WHERE id = $7";
        return request.bindOn(handle.createUpdate(sql))
            .execute()
            .next();
    }
}

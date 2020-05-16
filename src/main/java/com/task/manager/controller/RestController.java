package com.task.manager.controller;

import com.task.manager.logic.CreateTaskOperation;
import com.task.manager.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.manager.logic.EditTaskOperation;
import com.task.manager.logic.EditTaskOperation.EditTaskRequest;
import com.task.manager.logic.FindTasksOperation;
import com.task.manager.logic.FindTasksOperation.FindTasksRequest;
import com.task.manager.logic.GetTaskOperation;
import com.task.manager.logic.GetTaskOperation.GetTaskRequest;
import com.task.manager.logic.UpdateStatusOperation;
import com.task.manager.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.manager.model.Status;
import com.task.manager.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class RestController {

    private final CreateTaskOperation createTaskOperation;
    private final FindTasksOperation findTasksOperation;
    private final GetTaskOperation getTaskOperation;
    private final EditTaskOperation editTaskOperation;
    private final UpdateStatusOperation updateStatusOperation;

    @GetMapping("/tasks")
    public Flux<Task> findTasks(@Valid @RequestBody FindTasksRequest request) {
        return Mono.just(request)
            .flatMapMany(findTasksOperation::process);
    }

    @GetMapping("/task/{id}")
    public Mono<Task> getTaskById(@PathVariable(value = "id") Long taskId) {
        return Mono.just(new GetTaskRequest(taskId))
            .flatMap(getTaskOperation::process);
    }

    @PostMapping("/task")
    public Mono<Long> createTask(@Valid @RequestBody Task task) {
        return Mono.just(new CreateTaskRequest(task))
            .flatMap(createTaskOperation::process);
    }

    @PutMapping("/task/{id}")
    public Mono<Void> editTask(@PathVariable(value = "id") Long id, @Valid @RequestBody Task task) {
        if (!id.equals(task.id)) {
            return Mono.error(new RuntimeException("Id in path and in the body don't match"));
        }
        return Mono.just(new EditTaskRequest(task, id))
            .flatMap(editTaskOperation::process);
    }

    @PatchMapping("/task/{id}")
    public Mono<Void> updateStatus(@PathVariable(value = "id") Long id, @RequestParam("status") Status status) {
        return Mono.just(new UpdateStatusRequest(id, status))
            .flatMap(updateStatusOperation::process);
    }
}


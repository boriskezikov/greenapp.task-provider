package com.task.manager.controller;

import com.task.manager.logic.AttachPhotosOperation;
import com.task.manager.logic.AttachPhotosOperation.AttachPhotosRequest;
import com.task.manager.logic.CreateTaskOperation;
import com.task.manager.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.manager.logic.EditTaskOperation;
import com.task.manager.logic.EditTaskOperation.EditTaskRequest;
import com.task.manager.logic.FindAttachmentsByIdOperation;
import com.task.manager.logic.FindAttachmentsByIdOperation.FindAttachmentsByTaskIdRequest;
import com.task.manager.logic.FindTaskByIdOperation;
import com.task.manager.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.manager.logic.FindTasksOperation;
import com.task.manager.logic.FindTasksOperation.FindTasksRequest;
import com.task.manager.logic.UpdateStatusOperation;
import com.task.manager.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.manager.model.Status;
import com.task.manager.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/task-provider/")
@RequiredArgsConstructor
public class RestController {

    private final CreateTaskOperation createTaskOperation;
    private final FindTasksOperation findTasksOperation;
    private final FindTaskByIdOperation findTaskByIdOperation;
    private final FindAttachmentsByIdOperation findAttachmentsByIdOperation;
    private final EditTaskOperation editTaskOperation;
    private final UpdateStatusOperation updateStatusOperation;
    private final AttachPhotosOperation attachPhotosOperation;

    @GetMapping("/tasks")
    public Flux<Task> findTasks(@Valid @RequestBody FindTasksRequest request) {
        return Mono.just(request)
                .flatMapMany(findTasksOperation::process);
    }

    @GetMapping("/task/{id}")
    public Mono<Task> getTaskById(@PathVariable(value = "id") Long taskId) {
        return Mono.just(new FindTaskByIdRequest(taskId))
                .flatMap(findTaskByIdOperation::process);
    }

    @PostMapping("/task")
    public Mono<Long> createTask(@Valid @RequestBody Task task) {
        return Mono.just(new CreateTaskRequest(task))
                .flatMap(createTaskOperation::process);
    }

    @PutMapping("/task/{id}")
    public Mono<Void> editTask(@PathVariable(value = "id") Long id, @Valid @RequestBody Task task) {
        return Mono.just(new EditTaskRequest(task, id))
                .flatMap(editTaskOperation::process);
    }

    @PatchMapping("/task/{id}")
    public Mono<Void> updateStatus(@PathVariable(value = "id") Long id, @RequestParam("status") Status status) {
        return Mono.just(new UpdateStatusRequest(id, status))
                .flatMap(updateStatusOperation::process);
    }

    @PostMapping(value = "/task/{id}/attachment", consumes = {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE
    })
    public Mono<Void> attachPhotos(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Content-Length") Long length,
            @PathVariable(value = "id") Long id,
            @RequestBody byte[] content) {
        return Mono.just(new AttachPhotosRequest(id, contentType, length, content))
                .flatMap(attachPhotosOperation::process);
    }

    @GetMapping(value = "/task/{id}/attachment", produces = {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE
    })
    @ResponseBody
    public Flux<byte[]> findAttachments(@PathVariable(value = "id") Long id) {
        return Mono.just(new FindAttachmentsByTaskIdRequest(id))
                .flatMapMany(findAttachmentsByIdOperation::process)
                .map(a -> a.content);
    }

    @GetMapping("/test")
    public String test(){
        return "Hello task manager";
    }
}


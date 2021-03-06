package com.task.provider.controller;

import com.task.provider.logic.CreateTaskOperation;
import com.task.provider.logic.CreateTaskOperation.AttachPhotosRequest;
import com.task.provider.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.provider.logic.EditTaskOperation;
import com.task.provider.logic.EditTaskOperation.EditTaskRequest;
import com.task.provider.logic.FindAttachmentByIdOperation;
import com.task.provider.logic.FindAttachmentByIdOperation.FindAttachmentsByIdRequest;
import com.task.provider.logic.FindAttachmentsByTaskIdOperation;
import com.task.provider.logic.FindAttachmentsByTaskIdOperation.FindAttachmentsByTaskIdRequest;
import com.task.provider.logic.FindTaskByIdOperation;
import com.task.provider.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.provider.logic.FindTasksOperation;
import com.task.provider.logic.FindTasksOperation.FindTasksRequest;
import com.task.provider.logic.UpdateStatusOperation;
import com.task.provider.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.provider.model.Status;
import com.task.provider.model.Task;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import javax.validation.Valid;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/task-provider")
@RequiredArgsConstructor
public class RestController {

    private final static Logger log = LoggerFactory.getLogger(RestController.class);

    private final CreateTaskOperation createTaskOperation;
    private final FindTasksOperation findTasksOperation;
    private final FindTaskByIdOperation findTaskByIdOperation;
    private final FindAttachmentByIdOperation findAttachmentByIdOperation;
    private final FindAttachmentsByTaskIdOperation findAttachmentsByTaskIdOperation;
    private final EditTaskOperation editTaskOperation;
    private final UpdateStatusOperation updateStatusOperation;

    @PostMapping("/tasks")
    public Flux<Task> findTasks(@Valid @RequestBody FindTasksRequest request) {
        return Mono.just(request)
            .flatMapMany(findTasksOperation::process)
            .doOnSubscribe(s -> log.info("RestController.findTasks.in request = {}", request))
            .doOnComplete(() -> log.info("RestController.findTasks.out"));
    }

    @GetMapping("/task/{id}")
    public Mono<Task> getTaskById(@PathVariable(value = "id") Long taskId) {
        return Mono.just(new FindTaskByIdRequest(taskId))
            .flatMap(findTaskByIdOperation::process)
            .doOnSubscribe(s -> log.info("RestController.getTaskById.in id = {}", taskId))
            .doOnSuccess(s -> log.info("RestController.getTaskById.out"));
    }

    @PostMapping(value = "/task",
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        })
    public Mono<Long> createTask(@RequestPart("task") Task task,
                                 @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment) {
        var request = Flux.fromIterable(attachment)
            .flatMap(a -> {
                try {
                    return Mono.just(new AttachPhotosRequest(a.getContentType(), a.getSize(), a.getBytes()));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            }).collectList();
        return request.map(a -> new CreateTaskRequest(task, a))
            .flatMap(createTaskOperation::process)
            .doOnSubscribe(s -> log.info("RestController.createTask.in task = {}", task))
            .doOnSuccess(s -> log.info("RestController.createTask.out"));
    }

    @PutMapping(value = "/task", params = "detach")
    public Mono<Void> editTask(@RequestPart("task") Task task,
                               @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment,
                               @RequestParam(value = "detach") boolean detach) {
        var request = Flux.fromIterable(attachment)
            .flatMap(a -> {
                try {
                    return Mono.just(new AttachPhotosRequest(a.getContentType(), a.getSize(), a.getBytes()))
                        .map(r -> {
                            r.setTaskId(task.id);
                            return r;
                        });
                } catch (IOException e) {
                    return Mono.error(e);
                }
            }).collectList();
        return request.map(a -> new EditTaskRequest(task, a, detach))
            .flatMap(editTaskOperation::process)
            .doOnSubscribe(s -> log.info("RestController.editTask.in task = {}", task))
            .doOnSuccess(s -> log.info("RestController.editTask.out"));
    }

    @PatchMapping("/task/{id}")
    public Mono<Void> updateStatus(@PathVariable(value = "id") Long id, @RequestParam("status") Status status) {
        return Mono.just(new UpdateStatusRequest(id, status))
            .flatMap(updateStatusOperation::process)
            .doOnSubscribe(s -> log.info("RestController.updateStatus.in id = {}, status = {}", id, status))
            .doOnSuccess(s -> log.info("RestController.updateStatus.out"));
    }

    @GetMapping(value = "/task/{id}/attachment", produces = {
        MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @ResponseBody
    public Mono<MultiValueMap<String, HttpEntity<?>>> findAttachments(@PathVariable(value = "id") Long id) {
        return Mono.just(new FindAttachmentsByTaskIdRequest(id))
            .flatMapMany(findAttachmentsByTaskIdOperation::process)
            .collectList()
            .map(l -> {
                var builder = new MultipartBodyBuilder();
                l.forEach(a -> builder.part(
                    "attachment",
                    a.content,
                    MediaType.valueOf(a.contentType)
                ));
                return builder.build();
            })
            .doOnSubscribe(s -> log.info("RestController.findAttachments.in id = {}", id))
            .doOnSuccess(s -> log.info("RestController.findAttachments.out"));
    }

    @GetMapping(value = "/attachment/{id}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public Mono<byte[]> findAttachmentsById(@PathVariable(value = "id") Long id) {
        return Mono.just(new FindAttachmentsByIdRequest(id))
            .flatMap(findAttachmentByIdOperation::process)
            .map(a -> a.content)
            .doOnSubscribe(s -> log.info("RestController.findAttachments.in id = {}", id))
            .doOnSuccess(s -> log.info("RestController.findAttachments.out"));
    }
}

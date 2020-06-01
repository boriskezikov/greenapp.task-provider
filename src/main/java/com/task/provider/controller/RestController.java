package com.task.provider.controller;

import com.task.provider.logic.CreateTaskOperation;
import com.task.provider.logic.CreateTaskOperation.AttachPhotosRequest;
import com.task.provider.logic.CreateTaskOperation.CreateTaskRequest;
import com.task.provider.logic.EditTaskOperation;
import com.task.provider.logic.EditTaskOperation.EditTaskRequest;
import com.task.provider.logic.FindAttachmentsByIdOperation;
import com.task.provider.logic.FindAttachmentsByIdOperation.FindAttachmentsByTaskIdRequest;
import com.task.provider.logic.FindTaskByIdOperation;
import com.task.provider.logic.FindTaskByIdOperation.FindTaskByIdRequest;
import com.task.provider.logic.FindTasksOperation;
import com.task.provider.logic.FindTasksOperation.FindTasksRequest;
import com.task.provider.logic.UpdateStatusOperation;
import com.task.provider.logic.UpdateStatusOperation.UpdateStatusRequest;
import com.task.provider.model.Status;
import com.task.provider.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
@RequestMapping("task-provider/")
@RequiredArgsConstructor
public class RestController {

    private final CreateTaskOperation createTaskOperation;
    private final FindTasksOperation findTasksOperation;
    private final FindTaskByIdOperation findTaskByIdOperation;
    private final FindAttachmentsByIdOperation findAttachmentsByIdOperation;
    private final EditTaskOperation editTaskOperation;
    private final UpdateStatusOperation updateStatusOperation;

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

    @PostMapping(value = "/task",
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        })
    public Mono<Void> createTask(@RequestPart("task") Task task,
                                 @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment) {
        var request = Flux.fromIterable(attachment)
            .flatMap(a -> {
                try {
                    return Mono.just(new AttachPhotosRequest(a.getContentType(), a.getSize(), a.getBytes()));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            })
            .collectList();
        return request.map(a -> new CreateTaskRequest(task, a))
            .flatMap(createTaskOperation::process);
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
            })
            .collectList();
        return request.map(a -> new EditTaskRequest(task, a, detach))
            .flatMap(editTaskOperation::process);
    }

    @PatchMapping("/task/{id}")
    public Mono<Void> updateStatus(@PathVariable(value = "id") Long id, @RequestParam("status") Status status) {
        return Mono.just(new UpdateStatusRequest(id, status))
            .flatMap(updateStatusOperation::process);
    }

    @GetMapping(value = "/task/{id}/attachment", produces = {
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE
    })
    @ResponseBody
    public Mono<Void> findAttachments(@PathVariable(value = "id") Long id, ServerHttpResponse response) throws IOException {
        return Mono.just(new FindAttachmentsByTaskIdRequest(id))
            .flatMapMany(findAttachmentsByIdOperation::process)
            .collectList()
            .flatMap(l -> {
                response.getHeaders().setContentType(MediaType.valueOf(l.get(0).contentType));
                var zeroCopyResponse = (StreamingHttpOutputMessage) response;
                zeroCopyResponse.setBody(b -> {
                    b.write(l.get(0).content);
                    b.flush();
                });
                return null;
            }).then();
    }
}

package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.tasks.EffortPointsDto;
import com.terridb.chorequest.dtos.tasks.TaskPatchDto;
import com.terridb.chorequest.dtos.tasks.TaskRequestDto;
import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.services.TaskService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/efforts")
    public ResponseEntity<List<EffortPointsDto>> getEfforts() {
        return ResponseEntity.ok(taskService.getEfforts());
    }

    @GetMapping("/households/{householdId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksForHousehold(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskService.getTasksForHousehold(householdId, jwt.getSubject()));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskService.getTaskById(id, jwt.getSubject()));
    }

    @PostMapping("/households/{householdId}/tasks")
    public ResponseEntity<TaskResponseDto> postTask(@PathVariable UUID householdId,
                                                    @Valid @RequestBody TaskRequestDto input,
                                                    @AuthenticationPrincipal Jwt jwt) {
        TaskResponseDto dto = taskService.postTask(householdId, input, jwt.getSubject());
        URI location = UrlUtil.buildCreatedUri("/api/tasks/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskPatchDto input, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskService.updateTask(id, input, jwt.getSubject()));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        taskService.deleteTask(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

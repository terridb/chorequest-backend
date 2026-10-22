package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionRequestDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionSimpleResponseDto;
import com.terridb.chorequest.services.TaskCompletionService;
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
public class TaskCompletionController {
    private final TaskCompletionService taskCompletionService;

    public TaskCompletionController(TaskCompletionService taskCompletionService) {
        this.taskCompletionService = taskCompletionService;
    }

    @GetMapping("/completions/{id}")
    public ResponseEntity<TaskCompletionResponseDto> getCompletionById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskCompletionService.getCompletionById(id, jwt.getSubject()));
    }

    @GetMapping("/members/{id}/completions")
    public ResponseEntity<List<TaskCompletionResponseDto>> getCompletionsByMember(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskCompletionService.getCompletionsByMember(id, jwt.getSubject()));
    }

    @PostMapping("/assignments/{id}/completions")
    public ResponseEntity<TaskCompletionResponseDto> markAssignmentAsDone(@PathVariable UUID id, @Valid @RequestBody TaskCompletionRequestDto input, @AuthenticationPrincipal Jwt jwt) {
        TaskCompletionResponseDto dto = taskCompletionService.markAssignmentAsDone(id, input, jwt.getSubject());

        URI location = UrlUtil.buildCreatedUri("/api/completions/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/goals/{id}/completions")
    public ResponseEntity<List<TaskCompletionSimpleResponseDto>> getCompletionsForGoal(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskCompletionService.getCompletionsForGoal(id, jwt.getSubject()));
    }

    @GetMapping("/tasks/{id}/completions")
    public ResponseEntity<List<TaskCompletionSimpleResponseDto>> getCompletionHistoryForTask(@PathVariable UUID id,
                                                                                             @RequestParam(defaultValue = "10") int limit,
                                                                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskCompletionService.getCompletionHistoryForTask(id, limit, jwt.getSubject()));
    }
}


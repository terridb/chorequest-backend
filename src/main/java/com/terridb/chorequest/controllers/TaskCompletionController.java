package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionRequestDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.services.TaskCompletionService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TaskCompletionResponseDto> getCompletionById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskCompletionService.getCompletionById(id));
    }

    @GetMapping("/members/{id}/completions")
    public ResponseEntity<List<TaskCompletionResponseDto>> getCompletionsByMember(@PathVariable UUID id) {
        return ResponseEntity.ok(taskCompletionService.getCompletionsByMember(id));
    }

    @PostMapping("/assignments/{id}/completions")
    public ResponseEntity<TaskCompletionResponseDto> markAssignmentAsDone(@PathVariable UUID id, @Valid @RequestBody TaskCompletionRequestDto input) {
        TaskCompletionResponseDto dto = taskCompletionService.markAssignmentAsDone(id, input);

        URI location = UrlUtil.buildCreatedUri("/api/completions/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }
}


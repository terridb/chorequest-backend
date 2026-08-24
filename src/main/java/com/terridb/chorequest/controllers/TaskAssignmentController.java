package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentRequestDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.services.TaskAssignmentService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentController(TaskAssignmentService taskAssignmentService) {
        this.taskAssignmentService = taskAssignmentService;
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<TaskAssignmentResponseDto> getAssignmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentById(id));
    }

    @GetMapping("/tasks/{id}/assignments")
    public ResponseEntity<List<TaskAssignmentResponseDto>> getAssignmentsForTask(@PathVariable UUID id) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentsForTask(id));
    }

    @GetMapping("/members/{id}/assignments")
    public ResponseEntity<List<TaskAssignmentResponseDto>> getAssignmentsForMember(@PathVariable UUID id) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentsForMember(id));
    }

    @PostMapping("/tasks/{taskId}/members/{memberId}/assignments")
    public ResponseEntity<TaskAssignmentResponseDto> assignTaskToMember(@PathVariable UUID taskId,
                                                                        @PathVariable UUID memberId) {
        TaskAssignmentResponseDto dto = taskAssignmentService.assignTaskToMember(taskId, memberId);
        URI location = UrlUtil.buildCreatedUri("/api/assignments/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<TaskAssignmentResponseDto> updateAssignment(@PathVariable UUID id, @Valid @RequestBody TaskAssignmentRequestDto input) {
        return ResponseEntity.ok(taskAssignmentService.updateAssignment(id, input));
    }

}

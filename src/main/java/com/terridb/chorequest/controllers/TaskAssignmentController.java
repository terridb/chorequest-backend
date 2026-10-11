package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentRequestDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.services.TaskAssignmentService;
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
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentController(TaskAssignmentService taskAssignmentService) {
        this.taskAssignmentService = taskAssignmentService;
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<TaskAssignmentResponseDto> getAssignmentById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentById(id, jwt.getSubject()));
    }

    @GetMapping("/tasks/{id}/assignments")
    public ResponseEntity<List<TaskAssignmentResponseDto>> getAssignmentsForTask(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentsForTask(id, jwt.getSubject()));
    }

    @GetMapping("/members/{id}/assignments")
    public ResponseEntity<List<TaskAssignmentResponseDto>> getAssignmentsForMember(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskAssignmentService.getAssignmentsForMember(id, jwt.getSubject()));
    }

    @GetMapping("/households/{id}/assignments/open")
    public ResponseEntity<List<TaskAssignmentResponseDto>> getOpenAssignmentsForHousehold(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskAssignmentService.getOpenAssignmentsForHousehold(id, jwt.getSubject()));
    }

    @PostMapping("/tasks/{taskId}/members/{memberId}/assignments")
    public ResponseEntity<TaskAssignmentResponseDto> assignTaskToMember(@PathVariable UUID taskId,
                                                                        @PathVariable UUID memberId,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        TaskAssignmentResponseDto dto = taskAssignmentService.assignTaskToMember(taskId, memberId, jwt.getSubject());
        URI location = UrlUtil.buildCreatedUri("/api/assignments/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<TaskAssignmentResponseDto> updateAssignment(@PathVariable UUID id, @Valid @RequestBody TaskAssignmentRequestDto input, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(taskAssignmentService.updateAssignment(id, input, jwt.getSubject()));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        taskAssignmentService.deleteAssignment(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

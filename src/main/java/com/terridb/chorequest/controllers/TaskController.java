package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/households/{householdId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksForHousehold(@PathVariable UUID householdId) {

    }
}

package com.terridb.chorequest.schedulers;

import com.terridb.chorequest.services.TaskAssignmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskAssignmentScheduler {

    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentScheduler(TaskAssignmentService taskAssignmentService) {
        this.taskAssignmentService = taskAssignmentService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void generateDueAssignments() {
        taskAssignmentService.generateDueAssignments();
    }
}

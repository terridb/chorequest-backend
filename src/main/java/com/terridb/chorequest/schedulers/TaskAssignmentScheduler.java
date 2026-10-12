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

    // Households each have their own midnight, so check every 15 minutes (covers half- and quarter-hour offsets);
    // generateDueAssignments skips tasks that already have an open assignment, so repeated runs are harmless
    @Scheduled(cron = "0 */15 * * * *")
    public void generateDueAssignments() {
        taskAssignmentService.generateDueAssignments();
    }
}

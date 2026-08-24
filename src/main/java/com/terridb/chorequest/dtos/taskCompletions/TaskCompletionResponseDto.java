package com.terridb.chorequest.dtos.taskCompletions;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskCompletionResponseDto {
    private UUID id;
    private LocalDateTime completedAt;
    private int rewardedPoints;
    private TaskAssignmentResponseDto taskAssignment;
    private UserResponseSimpleDto completedBy;
}

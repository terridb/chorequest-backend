package com.terridb.chorequest.dtos.taskAssignments;

import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import com.terridb.chorequest.entities.enums.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskAssignmentResponseDto {
    private UUID id;
    private TaskResponseDto task;
    private UserResponseSimpleDto user;
    private LocalDate dueDate;
    private AssignmentStatus status;
    private LocalDateTime skippedAt;
    private UserResponseSimpleDto skippedBy;
}

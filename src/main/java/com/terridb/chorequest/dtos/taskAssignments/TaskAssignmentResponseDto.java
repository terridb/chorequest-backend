package com.terridb.chorequest.dtos.taskAssignments;

import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskAssignmentResponseDto {
    private TaskResponseDto task;
    private UserResponseSimpleDto user;
    private LocalDate dueDate;
}

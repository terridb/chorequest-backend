package com.terridb.chorequest.dtos.goals;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionSimpleResponseDto;
import com.terridb.chorequest.entities.enums.GoalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GoalResponseDto {
    private UUID id;
    private UUID householdId;
    private int goalPoints;
    private String title;
    private String description;
    private LocalDateTime completionDate;
    private int currentPoints;
    private GoalStatus status;
    private List<TaskCompletionSimpleResponseDto> contributions;
}

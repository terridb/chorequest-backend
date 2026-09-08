package com.terridb.chorequest.dtos.goals;

import com.terridb.chorequest.entities.enums.GoalStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GoalSimpleResponseDto {
    private UUID id;
    private UUID householdId;
    private int goalPoints;
    private String title;
    private GoalStatus status;
}

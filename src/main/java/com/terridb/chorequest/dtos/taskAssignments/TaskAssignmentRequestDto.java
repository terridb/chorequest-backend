package com.terridb.chorequest.dtos.taskAssignments;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskAssignmentRequestDto {
    @NotNull
    private UUID householdMemberId;
}

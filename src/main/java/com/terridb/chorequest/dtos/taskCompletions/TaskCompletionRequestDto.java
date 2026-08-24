package com.terridb.chorequest.dtos.taskCompletions;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskCompletionRequestDto {
    @NotNull
    private UUID completedByMemberId;
}

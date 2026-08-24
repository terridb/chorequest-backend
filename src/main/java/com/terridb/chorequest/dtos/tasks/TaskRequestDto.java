package com.terridb.chorequest.dtos.tasks;

import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.Effort;
import com.terridb.chorequest.entities.enums.Frequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TaskRequestDto {
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Effort is required")
    private Effort effort;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Starting date is required")
    private LocalDate startingDate;

    private boolean alwaysSameDay;

    @PositiveOrZero(message = "Default points must be zero or positive")
    @Max(value = 10, message = "Default points must be at most 10")
    private int defaultPoints;

    @NotNull(message = "Assignment strategy is required")
    private AssignmentStrategy strategy;

    private UUID responsibleMemberId;
}

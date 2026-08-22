package com.terridb.chorequest.dtos.tasks;

import com.terridb.chorequest.entities.enums.Effort;
import com.terridb.chorequest.entities.enums.Frequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskPatchDto {
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Effort effort;

    private Frequency frequency;

    private LocalDate startingDate;

    private Boolean alwaysSameDay;

    @PositiveOrZero(message = "Default points must be zero or positive")
    @Max(value = 10, message = "Default points must be at most 10")
    private Integer defaultPoints;
}

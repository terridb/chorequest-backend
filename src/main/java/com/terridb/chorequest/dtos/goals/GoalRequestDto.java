package com.terridb.chorequest.dtos.goals;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalRequestDto {
    @Positive(message = "Goal points must be greater than 0")
    private int goalPoints;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;
}

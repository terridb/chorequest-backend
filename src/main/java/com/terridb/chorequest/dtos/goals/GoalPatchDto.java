package com.terridb.chorequest.dtos.goals;

import com.terridb.chorequest.entities.enums.GoalStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoalPatchDto {
    @Positive(message = "Goal points must be greater than 0")
    private Integer goalPoints;

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private GoalStatus status;
}

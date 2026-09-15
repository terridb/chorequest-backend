package com.terridb.chorequest.dtos.tasks;

import com.terridb.chorequest.entities.enums.Effort;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EffortPointsDto {
    private Effort effort;
    private int points;
}

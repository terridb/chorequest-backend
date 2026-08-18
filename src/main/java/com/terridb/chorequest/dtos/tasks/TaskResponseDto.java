package com.terridb.chorequest.dtos.tasks;

import com.terridb.chorequest.entities.enums.Effort;
import com.terridb.chorequest.entities.enums.Frequency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TaskResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Effort effort;
    private Frequency frequency;
    private LocalDate startingDate;
    private boolean alwaysSameDay;
    private int defaultPoints;
    private UUID householdId;
}

package com.terridb.chorequest.dtos.taskAssignments;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.enums.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskHistoryEntryDto {
    private UUID assignmentId;
    private AssignmentStatus status;
    private HouseholdMemberResponseDto member;
    private int rewardedPoints;
    private LocalDateTime occurredAt;
    private LocalDate dueDate;
}

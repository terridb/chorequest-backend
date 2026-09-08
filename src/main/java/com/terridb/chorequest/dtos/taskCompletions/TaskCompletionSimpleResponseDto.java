package com.terridb.chorequest.dtos.taskCompletions;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskCompletionSimpleResponseDto {
    private UUID id;
    private HouseholdMemberResponseDto member;
    private int rewardedPoints;
}

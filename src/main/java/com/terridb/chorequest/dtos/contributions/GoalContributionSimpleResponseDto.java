package com.terridb.chorequest.dtos.contributions;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GoalContributionSimpleResponseDto {
    private UUID id;
    private HouseholdMemberResponseDto member;
    private int points;
}

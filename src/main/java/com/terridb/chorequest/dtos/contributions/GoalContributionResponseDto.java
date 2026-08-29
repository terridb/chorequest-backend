package com.terridb.chorequest.dtos.contributions;

import com.terridb.chorequest.dtos.goals.GoalSimpleResponseDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GoalContributionResponseDto {
    private UUID id;
    private GoalSimpleResponseDto goal;
    private HouseholdMemberResponseDto member;
    private int points;
}

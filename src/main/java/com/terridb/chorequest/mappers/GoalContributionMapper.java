package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.contributions.GoalContributionResponseDto;
import com.terridb.chorequest.dtos.contributions.GoalContributionSimpleResponseDto;
import com.terridb.chorequest.dtos.goals.GoalSimpleResponseDto;
import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.GoalContribution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalContributionMapper {
    private final HouseholdMemberMapper householdMemberMapper;

    public GoalContributionMapper(HouseholdMemberMapper householdMemberMapper) {
        this.householdMemberMapper = householdMemberMapper;
    }

    public GoalContributionResponseDto toDto(GoalContribution contribution) {
        GoalContributionResponseDto responseDto = new GoalContributionResponseDto();

        responseDto.setId(contribution.getId());
        responseDto.setGoal(toSimpleGoalDto(contribution.getGoal()));
        responseDto.setMember(householdMemberMapper.toDto(contribution.getHouseholdMember()));
        responseDto.setPoints(contribution.getPoints());

        return responseDto;
    }

    public List<GoalContributionResponseDto> toDto(List<GoalContribution> contributions) {
        return contributions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GoalContributionSimpleResponseDto toSimpleDto(GoalContribution contribution) {
        GoalContributionSimpleResponseDto responseDto = new GoalContributionSimpleResponseDto();

        responseDto.setId(contribution.getId());
        responseDto.setMember(householdMemberMapper.toDto(contribution.getHouseholdMember()));
        responseDto.setPoints(contribution.getPoints());

        return responseDto;
    }

    public List<GoalContributionSimpleResponseDto> toSimpleDto(List<GoalContribution> contributions) {
        return contributions.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }

    private GoalSimpleResponseDto toSimpleGoalDto(Goal goal) {
        GoalSimpleResponseDto goalDto = new GoalSimpleResponseDto();

        goalDto.setId(goal.getId());
        goalDto.setHouseholdId(goal.getHousehold().getId());
        goalDto.setGoalPoints(goal.getGoalPoints());
        goalDto.setTitle(goal.getTitle());
        goalDto.setStatus(goal.getStatus());

        return goalDto;
    }
}

package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.goals.GoalPatchDto;
import com.terridb.chorequest.dtos.goals.GoalRequestDto;
import com.terridb.chorequest.dtos.goals.GoalResponseDto;
import com.terridb.chorequest.dtos.goals.GoalSimpleResponseDto;
import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.TaskCompletion;
import com.terridb.chorequest.entities.enums.GoalStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalMapper {
    private final TaskCompletionMapper taskCompletionMapper;

    public GoalMapper(TaskCompletionMapper taskCompletionMapper) {
        this.taskCompletionMapper = taskCompletionMapper;
    }

    public Goal toEntity(GoalRequestDto requestDto, Goal goal) {
        if (goal == null) {
            goal = new Goal();
        }

        goal.setGoalPoints(requestDto.getGoalPoints());
        goal.setTitle(requestDto.getTitle());
        goal.setDescription(requestDto.getDescription());

        return goal;
    }

    public Goal patch(GoalPatchDto patchDto, Goal goal) {
        if (patchDto.getGoalPoints() != null) {
            goal.setGoalPoints(patchDto.getGoalPoints());
        }
        if (patchDto.getTitle() != null) {
            goal.setTitle(patchDto.getTitle());
        }
        if (patchDto.getDescription() != null) {
            goal.setDescription(patchDto.getDescription());
        }
        if (patchDto.getStatus() != null) {
            goal.setStatus(patchDto.getStatus());
            if (patchDto.getStatus() != GoalStatus.ACTIVE) {
                goal.setCompletionDate(LocalDateTime.now());
            }
        }

        return goal;
    }

    public GoalResponseDto toDto(Goal goal) {
        GoalResponseDto responseDto = new GoalResponseDto();

        responseDto.setId(goal.getId());
        responseDto.setHouseholdId(goal.getHousehold().getId());
        responseDto.setGoalPoints(goal.getGoalPoints());
        responseDto.setTitle(goal.getTitle());
        responseDto.setDescription(goal.getDescription());
        responseDto.setCompletionDate(goal.getCompletionDate());
        responseDto.setCurrentPoints(goal.getCurrentPoints());
        responseDto.setStatus(goal.getStatus());
        List<TaskCompletion> contributions = goal.getContributions().stream()
                .sorted(Comparator.comparingInt(TaskCompletion::getRewardedPoints).reversed())
                .collect(Collectors.toList());
        responseDto.setContributions(taskCompletionMapper.toSimpleDto(contributions));

        return responseDto;
    }

    public List<GoalResponseDto> toDto(List<Goal> goals) {
        return goals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GoalSimpleResponseDto toSimpleDto(Goal goal) {
        GoalSimpleResponseDto responseDto = new GoalSimpleResponseDto();

        responseDto.setId(goal.getId());
        responseDto.setHouseholdId(goal.getHousehold().getId());
        responseDto.setGoalPoints(goal.getGoalPoints());
        responseDto.setTitle(goal.getTitle());
        responseDto.setStatus(goal.getStatus());

        return responseDto;
    }

    public List<GoalSimpleResponseDto> toSimpleDto(List<Goal> goals) {
        return goals.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }
}

package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.goals.GoalSimpleResponseDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionSimpleResponseDto;
import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.TaskCompletion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskCompletionMapper {
    private final TaskAssignmentMapper taskAssignmentMapper;
    private final UserMapper userMapper;
    private final HouseholdMemberMapper householdMemberMapper;

    public TaskCompletionMapper(TaskAssignmentMapper taskAssignmentMapper, UserMapper userMapper, HouseholdMemberMapper householdMemberMapper) {
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.userMapper = userMapper;
        this.householdMemberMapper = householdMemberMapper;
    }

    public TaskCompletionResponseDto toDto(TaskCompletion taskCompletion) {
        TaskCompletionResponseDto responseDto = new TaskCompletionResponseDto();

        responseDto.setId(taskCompletion.getId());
        responseDto.setCompletedAt(taskCompletion.getCompletedAt());
        responseDto.setRewardedPoints(taskCompletion.getRewardedPoints());
        responseDto.setTaskAssignment(taskAssignmentMapper.toDto(taskCompletion.getTaskAssignment()));
        responseDto.setCompletedBy(userMapper.toSimpleDto(taskCompletion.getCompletedBy().getUser()));
        if (taskCompletion.getGoal() != null) {
            responseDto.setGoal(toSimpleGoalDto(taskCompletion.getGoal()));
        }

        return responseDto;
    }

    public List<TaskCompletionResponseDto> toDto(List<TaskCompletion> taskCompletions) {
        return taskCompletions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TaskCompletionSimpleResponseDto toSimpleDto(TaskCompletion taskCompletion) {
        TaskCompletionSimpleResponseDto responseDto = new TaskCompletionSimpleResponseDto();

        responseDto.setId(taskCompletion.getId());
        responseDto.setMember(householdMemberMapper.toDto(taskCompletion.getCompletedBy()));
        responseDto.setRewardedPoints(taskCompletion.getRewardedPoints());
        responseDto.setCompletedAt(taskCompletion.getCompletedAt());

        return responseDto;
    }

    public List<TaskCompletionSimpleResponseDto> toSimpleDto(List<TaskCompletion> taskCompletions) {
        return taskCompletions.stream()
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

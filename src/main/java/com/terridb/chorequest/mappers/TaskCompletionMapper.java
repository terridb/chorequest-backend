package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.entities.TaskCompletion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskCompletionMapper {
    private final TaskAssignmentMapper taskAssignmentMapper;
    private final UserMapper userMapper;

    public TaskCompletionMapper(TaskAssignmentMapper taskAssignmentMapper, UserMapper userMapper) {
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.userMapper = userMapper;
    }

    public TaskCompletionResponseDto toDto(TaskCompletion taskCompletion) {
        TaskCompletionResponseDto responseDto = new TaskCompletionResponseDto();

        responseDto.setId(taskCompletion.getId());
        responseDto.setCompletedAt(taskCompletion.getCompletedAt());
        responseDto.setRewardedPoints(taskCompletion.getRewardedPoints());
        responseDto.setTaskAssignment(taskAssignmentMapper.toDto(taskCompletion.getTaskAssignment()));
        responseDto.setCompletedBy(userMapper.toSimpleDto(taskCompletion.getCompletedBy()));

        return responseDto;
    }

    public List<TaskCompletionResponseDto> toDto(List<TaskCompletion> taskCompletions) {
        return taskCompletions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.entities.TaskAssignment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskAssignmentMapper {
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public TaskAssignmentMapper(TaskMapper taskMapper, UserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    public TaskAssignmentResponseDto toDto(TaskAssignment taskAssignment) {
        TaskAssignmentResponseDto responseDto = new TaskAssignmentResponseDto();

        responseDto.setId(taskAssignment.getId());
        responseDto.setTask(taskMapper.toDto(taskAssignment.getTask()));
        responseDto.setUser(userMapper.toSimpleDto(taskAssignment.getHouseholdMember().getUser()));
        responseDto.setDueDate(taskAssignment.getDueDate());

        return responseDto;
    }

    public List<TaskAssignmentResponseDto> toDto(List<TaskAssignment> taskAssignments) {
        return taskAssignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskHistoryEntryDto;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.TaskCompletion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskAssignmentMapper {
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final HouseholdMemberMapper householdMemberMapper;

    public TaskAssignmentMapper(TaskMapper taskMapper, UserMapper userMapper, HouseholdMemberMapper householdMemberMapper) {
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.householdMemberMapper = householdMemberMapper;
    }

    public TaskAssignmentResponseDto toDto(TaskAssignment taskAssignment) {
        TaskAssignmentResponseDto responseDto = new TaskAssignmentResponseDto();

        responseDto.setId(taskAssignment.getId());
        responseDto.setTask(taskMapper.toDto(taskAssignment.getTask()));
        responseDto.setUser(userMapper.toSimpleDto(taskAssignment.getHouseholdMember().getUser()));
        responseDto.setDueDate(taskAssignment.getDueDate());
        responseDto.setStatus(taskAssignment.getStatus());
        responseDto.setSkippedAt(taskAssignment.getSkippedAt());
        if (taskAssignment.getSkippedBy() != null) {
            responseDto.setSkippedBy(userMapper.toSimpleDto(taskAssignment.getSkippedBy().getUser()));
        }

        return responseDto;
    }

    public List<TaskAssignmentResponseDto> toDto(List<TaskAssignment> taskAssignments) {
        return taskAssignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TaskHistoryEntryDto toHistoryDto(TaskAssignment taskAssignment) {
        TaskHistoryEntryDto responseDto = new TaskHistoryEntryDto();
        TaskCompletion completion = taskAssignment.getTaskCompletion();

        responseDto.setAssignmentId(taskAssignment.getId());
        responseDto.setStatus(taskAssignment.getStatus());
        responseDto.setDueDate(taskAssignment.getDueDate());
        switch (taskAssignment.getStatus()) {
            case COMPLETED -> {
                responseDto.setMember(householdMemberMapper.toDto(completion.getCompletedBy()));
                responseDto.setRewardedPoints(completion.getRewardedPoints());
                responseDto.setOccurredAt(completion.getCompletedAt());
            }
            case SKIPPED -> {
                responseDto.setMember(householdMemberMapper.toDto(taskAssignment.getSkippedBy()));
                responseDto.setOccurredAt(taskAssignment.getSkippedAt());
            }
            case OPEN -> throw new IllegalStateException("Open assignment " + taskAssignment.getId() + " has no history");
        }

        return responseDto;
    }

    public List<TaskHistoryEntryDto> toHistoryDto(List<TaskAssignment> taskAssignments) {
        return taskAssignments.stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }
}

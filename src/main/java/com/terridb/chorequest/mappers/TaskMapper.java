package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.tasks.TaskPatchDto;
import com.terridb.chorequest.dtos.tasks.TaskRequestDto;
import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.entities.Task;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {
    public Task toEntity(TaskRequestDto requestDto, Task task) {
        if (task == null) {
            task = new Task();
        }

        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setEffort(requestDto.getEffort());
        task.setFrequency(requestDto.getFrequency());
        task.setStartingDate(requestDto.getStartingDate());
        task.setAlwaysSameDay(requestDto.isAlwaysSameDay());
        task.setDefaultPoints(requestDto.getEffort().getPoints());
        task.setStrategy(requestDto.getStrategy());

        return task;
    }

    public Task patch(TaskPatchDto patchDto, Task task) {
        if (patchDto.getTitle() != null) {
            task.setTitle(patchDto.getTitle());
        }
        if (patchDto.getDescription() != null) {
            task.setDescription(patchDto.getDescription());
        }
        if (patchDto.getEffort() != null) {
            task.setEffort(patchDto.getEffort());
            task.setDefaultPoints(patchDto.getEffort().getPoints());
        }
        if (patchDto.getFrequency() != null) {
            task.setFrequency(patchDto.getFrequency());
        }
        if (patchDto.getStartingDate() != null) {
            task.setStartingDate(patchDto.getStartingDate());
        }
        if (patchDto.getAlwaysSameDay() != null) {
            task.setAlwaysSameDay(patchDto.getAlwaysSameDay());
        }
        if (patchDto.getStrategy() != null) {
            task.setStrategy(patchDto.getStrategy());
        }

        return task;
    }

    public TaskResponseDto toDto(Task task) {
        TaskResponseDto responseDto = new TaskResponseDto();

        responseDto.setId(task.getId());
        responseDto.setTitle(task.getTitle());
        responseDto.setDescription(task.getDescription());
        responseDto.setEffort(task.getEffort());
        responseDto.setFrequency(task.getFrequency());
        responseDto.setStartingDate(task.getStartingDate());
        responseDto.setAlwaysSameDay(task.isAlwaysSameDay());
        responseDto.setDefaultPoints(task.getDefaultPoints());
        responseDto.setStrategy(task.getStrategy());
        responseDto.setResponsibleMemberId(task.getResponsibleMember() != null ? task.getResponsibleMember().getId() : null);
        responseDto.setHouseholdId(task.getHousehold().getId());

        return responseDto;
    }

    public List<TaskResponseDto> toDto(List<Task> tasks) {
        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

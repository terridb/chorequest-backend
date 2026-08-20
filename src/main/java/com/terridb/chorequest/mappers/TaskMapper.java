package com.terridb.chorequest.mappers;

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
        task.setDefaultPoints(requestDto.getDefaultPoints());

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
        responseDto.setHouseholdId(task.getHousehold().getId());

        return responseDto;
    }

    public List<TaskResponseDto> toDto(List<Task> tasks) {
        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

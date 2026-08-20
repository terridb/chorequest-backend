package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskMapper;
import com.terridb.chorequest.repositories.HouseholdRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final HouseholdRepository householdRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, HouseholdRepository householdRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.householdRepository = householdRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponseDto> getTasksForHousehold(UUID householdId) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + householdId + " not found"));
        List<Task> tasks = taskRepository.findByHouseholdOrderByCreatedAtDesc(household);

        return taskMapper.toDto(tasks);
    }

//    @todo get tasks by user
//    @todo create new task by household ? or by user?
//    @todo adjust ask
//    @todo delete task
}

package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.tasks.TaskPatchDto;
import com.terridb.chorequest.dtos.tasks.TaskRequestDto;
import com.terridb.chorequest.dtos.tasks.TaskResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.HouseholdRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, HouseholdRepository householdRepository,
                        HouseholdMemberRepository householdMemberRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponseDto> getTasksForHousehold(UUID householdId) {
        Household household = getHousehold(householdId);
        List<Task> tasks = taskRepository.findByHouseholdOrderByCreatedAtDesc(household);

        return taskMapper.toDto(tasks);
    }

    public TaskResponseDto getTaskById(UUID id) {
        return taskMapper.toDto(getTask(id));
    }

    public TaskResponseDto postTask(UUID householdId, TaskRequestDto input) {
        Household household = getHousehold(householdId);

        Task task = taskMapper.toEntity(input, null);
        task.setHousehold(household);
        applyResponsibleMember(task, input.getStrategy(), input.getResponsibleMemberId());

        return taskMapper.toDto(taskRepository.save(task));
    }

    public TaskResponseDto updateTask(UUID id, TaskPatchDto input) {
        Task task =  getTask(id);
        Task patchedTask = taskMapper.patch(input, task);

        if (input.getStrategy() != null || input.getResponsibleMemberId() != null) {
            applyResponsibleMember(patchedTask, patchedTask.getStrategy(), input.getResponsibleMemberId());
        }

        return taskMapper.toDto(taskRepository.save(patchedTask));
    }

    public void deleteTask(UUID id) {
        taskRepository.delete(getTask(id));
    }

    private void applyResponsibleMember(Task task, AssignmentStrategy strategy, UUID responsibleMemberId) {
        if (strategy != AssignmentStrategy.SOLO) {
            task.setResponsibleMember(null);
            return;
        }

        if (responsibleMemberId == null) {
            throw new BadRequestException("responsibleMemberId is required when strategy is SOLO");
        }

        HouseholdMember member = householdMemberRepository.findById(responsibleMemberId)
                .orElseThrow(() -> new RecordNotFoundException("Household member with id " + responsibleMemberId + " not found"));
        task.setResponsibleMember(member);
    }

    private Household getHousehold(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + id + " not found"));
    }

    private Task getTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Task with id " + id + " not found"));

    }
}

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
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final TaskMapper taskMapper;
    private final HouseholdService householdService;

    public TaskService(TaskRepository taskRepository, HouseholdService householdService,
                        HouseholdMemberRepository householdMemberRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.householdService = householdService;
        this.householdMemberRepository = householdMemberRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponseDto> getTasksForHousehold(UUID householdId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        List<Task> tasks = taskRepository.findByHouseholdOrderByCreatedAtDesc(household);

        return taskMapper.toDto(tasks);
    }

    public TaskResponseDto getTaskById(UUID id, String sub) {
        Task task = getTask(id);
        householdService.requireMembership(task.getHousehold(), sub);

        return taskMapper.toDto(task);
    }

    public TaskResponseDto postTask(UUID householdId, TaskRequestDto input, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        Task task = taskMapper.toEntity(input, null);
        task.setHousehold(household);
        applyResponsibleMember(task, input.getStrategy(), input.getResponsibleMemberId());

        return taskMapper.toDto(taskRepository.save(task));
    }

    public TaskResponseDto updateTask(UUID id, TaskPatchDto input, String sub) {
        Task task =  getTask(id);
        householdService.requireMembership(task.getHousehold(), sub);
        Task patchedTask = taskMapper.patch(input, task);

        if (input.getStrategy() != null || input.getResponsibleMemberId() != null) {
            applyResponsibleMember(patchedTask, patchedTask.getStrategy(), input.getResponsibleMemberId());
        }

        return taskMapper.toDto(taskRepository.save(patchedTask));
    }

    public void deleteTask(UUID id, String sub) {
        Task task = getTask(id);
        householdService.requireMembership(task.getHousehold(), sub);

        taskRepository.delete(task);
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

        if (!member.getHousehold().getId().equals(task.getHousehold().getId())) {
            throw new BadRequestException("Household member does not belong to this task's household");
        }

        task.setResponsibleMember(member);
    }

    private Task getTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Task with id " + id + " not found"));

    }
}

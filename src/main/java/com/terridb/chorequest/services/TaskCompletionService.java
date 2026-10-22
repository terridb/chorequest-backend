package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionRequestDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionSimpleResponseDto;
import com.terridb.chorequest.entities.*;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskCompletionMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskCompletionRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskCompletionService {
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskCompletionMapper taskCompletionMapper;
    private final GoalService goalService;
    private final HouseholdService householdService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskService taskService;

    public TaskCompletionService(TaskAssignmentRepository taskAssignmentRepository, HouseholdMemberRepository householdMemberRepository, TaskCompletionRepository taskCompletionRepository, TaskCompletionMapper taskCompletionMapper, GoalService goalService, HouseholdService householdService, TaskAssignmentService taskAssignmentService, TaskService taskService) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.taskCompletionMapper = taskCompletionMapper;
        this.goalService = goalService;
        this.householdService = householdService;
        this.taskAssignmentService = taskAssignmentService;
        this.taskService = taskService;
    }

    @Transactional
    public TaskCompletionResponseDto markAssignmentAsDone(UUID id, TaskCompletionRequestDto input, String sub) {
        TaskAssignment assignment = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Assignment with id " + id + " not found"));
        householdService.requireMembership(assignment.getTask().getHousehold(), sub);

        HouseholdMember member = getMember(input.getCompletedByMemberId());

        if (assignment.getTaskCompletion() != null) {
            throw new BadRequestException("Assignment has already been completed");
        }

        if (!assignment.getTask().getHousehold().getId().equals(member.getHousehold().getId())) {
            throw new BadRequestException("Household member does not belong to this task's household");
        }

        if (!member.isActive()) {
            throw new BadRequestException("Household member has left the household");
        }

        TaskCompletion completion = new TaskCompletion();
        completion.setRewardedPoints(assignment.getTask().getDefaultPoints());
        completion.setTaskAssignment(assignment);
        completion.setCompletedBy(member);

        goalService.getActiveGoalForHousehold(member.getHousehold())
                .ifPresent(goal -> {
                    completion.setGoal(goal);
                    goalService.addPointsToGoal(goal, completion.getRewardedPoints());
                });

        member.setPoints(member.getPoints() + completion.getRewardedPoints());
        householdMemberRepository.save(member);

        TaskCompletion savedCompletion = taskCompletionRepository.save(completion);
        taskAssignmentService.createNextAssignment(assignment);

        return taskCompletionMapper.toDto(savedCompletion);
    }

    public TaskCompletionResponseDto getCompletionById(UUID id, String sub) {
        TaskCompletion completion = getCompletion(id);
        householdService.requireMembership(completion.getTaskAssignment().getTask().getHousehold(), sub);

        return taskCompletionMapper.toDto(completion);
    }

    public TaskCompletion getCompletion(UUID id) {
        return taskCompletionRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Completion with id " + id + " not found"));
    }

    public List<TaskCompletionResponseDto> getCompletionsByMember(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        householdService.requireMembership(member.getHousehold(), sub);

        List<TaskCompletion> completions = taskCompletionRepository.findByCompletedByOrderByCompletedAtDesc(member);
        return taskCompletionMapper.toDto(completions);
    }

    public List<TaskCompletionSimpleResponseDto> getCompletionsForGoal(UUID goalId, String sub) {
        Goal goal = goalService.getGoal(goalId);
        householdService.requireMembership(goal.getHousehold(), sub);

        List<TaskCompletion> completions = taskCompletionRepository.findByGoalOrderByRewardedPointsDesc(goal);
        return taskCompletionMapper.toSimpleDto(completions);
    }

    public List<TaskCompletionSimpleResponseDto> getCompletionHistoryForTask(UUID taskId, int limit, String sub) {
        if (limit < 1 || limit > 50) {
            throw new BadRequestException("Limit must be between 1 and 50");
        }

        Task task = taskService.getTask(taskId);
        householdService.requireMembership(task.getHousehold(), sub);

        List<TaskCompletion> completions = taskCompletionRepository.findByTaskAssignmentTaskOrderByCompletedAtDesc(task, Limit.of(limit));
        return taskCompletionMapper.toSimpleDto(completions);
    }

    private HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Member with id " + id + " not found"));
    }
}

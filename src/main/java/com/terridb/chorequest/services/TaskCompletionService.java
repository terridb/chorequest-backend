package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionRequestDto;
import com.terridb.chorequest.dtos.taskCompletions.TaskCompletionResponseDto;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.TaskCompletion;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskCompletionMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskCompletionRepository;
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

    public TaskCompletionService(TaskAssignmentRepository taskAssignmentRepository, HouseholdMemberRepository householdMemberRepository, TaskCompletionRepository taskCompletionRepository, TaskCompletionMapper taskCompletionMapper) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.taskCompletionMapper = taskCompletionMapper;
    }

    @Transactional
    public TaskCompletionResponseDto markAssignmentAsDone(UUID id, TaskCompletionRequestDto input) {
        TaskAssignment assignment = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Assignment with id " + id + " not found"));

        HouseholdMember member = getMember(input.getCompletedByMemberId());

        if (assignment.getTaskCompletion() != null) {
            throw new BadRequestException("Assignment has already been completed");
        }

        if (!assignment.getTask().getHousehold().getId().equals(member.getHousehold().getId())) {
            throw new BadRequestException("Household member does not belong to this task's household");
        }

        TaskCompletion completion = new TaskCompletion();
        completion.setRewardedPoints(assignment.getTask().getDefaultPoints());
        completion.setTaskAssignment(assignment);
        completion.setCompletedBy(member);

        member.setPoints(member.getPoints() + completion.getRewardedPoints());
        householdMemberRepository.save(member);

        TaskCompletion savedCompletion = taskCompletionRepository.save(completion);
        return taskCompletionMapper.toDto(savedCompletion);
    }

    public TaskCompletionResponseDto getCompletionById(UUID id) {
        TaskCompletion completion = taskCompletionRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Completion with id " + id + " not found"));

        return taskCompletionMapper.toDto(completion);
    }

    public List<TaskCompletionResponseDto> getCompletionsByMember(UUID id) {
        HouseholdMember member = getMember(id);
        List<TaskCompletion> completions = taskCompletionRepository.findByCompletedByOrderByCompletedAtDesc(member);
        return taskCompletionMapper.toDto(completions);
    }

    private HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Member with id " + id + " not found"));
    }
}

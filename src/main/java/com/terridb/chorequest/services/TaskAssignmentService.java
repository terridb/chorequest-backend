package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentRequestDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.Frequency;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskAssignmentMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskAssignmentService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskAssignmentMapper taskAssignmentMapper;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdService householdService;

    public TaskAssignmentService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository, TaskAssignmentMapper taskAssignmentMapper, HouseholdMemberRepository householdMemberRepository, HouseholdService householdService) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
    }

    public TaskAssignmentResponseDto getAssignmentById(UUID id, String sub) {
        TaskAssignment assignment = getAssignment(id);
        householdService.requireMembership(assignment.getTask().getHousehold(), sub);

        return taskAssignmentMapper.toDto(assignment);
    }

    public List<TaskAssignmentResponseDto> getAssignmentsForTask(UUID id, String sub) {
        Task task = getTask(id);
        householdService.requireMembership(task.getHousehold(), sub);

        Set<TaskAssignment> assignments = taskAssignmentRepository.findByTaskOrderByDueDateDesc(task);

        return taskAssignmentMapper.toDto(assignments.stream().toList());
    }

    public List<TaskAssignmentResponseDto> getAssignmentsForMember(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        householdService.requireMembership(member.getHousehold(), sub);

        Set<TaskAssignment> assignments = taskAssignmentRepository.findByHouseholdMemberOrderByDueDateDesc(member);

        return taskAssignmentMapper.toDto(assignments.stream().toList());
    }

    public List<TaskAssignmentResponseDto> getOpenAssignmentsForHousehold(UUID householdId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        Set<TaskAssignment> assignments = taskAssignmentRepository.findByTask_HouseholdAndTaskCompletionIsNullOrderByDueDateAsc(household);

        return taskAssignmentMapper.toDto(assignments.stream().toList());
    }

    public TaskAssignmentResponseDto updateAssignment(UUID id, TaskAssignmentRequestDto input, String sub) {
        TaskAssignment assignment = getAssignment(id);
        householdService.requireMembership(assignment.getTask().getHousehold(), sub);

        HouseholdMember member = getMember(input.getHouseholdMemberId());

        if (assignment.getTaskCompletion() != null) {
            throw new BadRequestException("Cannot reassign a completed task assignment");
        }

        if (!member.getHousehold().getId().equals(assignment.getTask().getHousehold().getId())) {
            throw new BadRequestException("Household member does not belong to this task's household");
        }

        assignment.setHouseholdMember(member);
        return taskAssignmentMapper.toDto(taskAssignmentRepository.save(assignment));
    }

    public void deleteAssignment(UUID id, String sub) {
        TaskAssignment assignment = getAssignment(id);
        householdService.requireMembership(assignment.getTask().getHousehold(), sub);

        if (assignment.getTaskCompletion() != null) {
            throw new BadRequestException("Cannot delete a completed task assignment");
        }

        taskAssignmentRepository.delete(assignment);
    }

    public TaskAssignmentResponseDto assignTaskToMember(UUID taskId, UUID memberId, String sub) {
        HouseholdMember member = getMember(memberId);
        Task task = getTask(taskId);
        householdService.requireMembership(task.getHousehold(), sub);

        if (!member.getHousehold().getId().equals(task.getHousehold().getId())) {
            throw new BadRequestException("Household member does not belong to this task's household");
        }

        return createAssignment(task, member, calculateTaskDueDate(task));
    }

    public void generateDueAssignments() {
        List<Task> tasks = taskRepository.findByFrequencyNot(Frequency.AS_NEEDED);

        for (Task task : tasks) {
            if (taskAssignmentRepository.existsByTaskAndTaskCompletionIsNull(task)) {
                continue;
            }

            LocalDate dueDate = calculateTaskDueDate(task);
            if (dueDate == null || dueDate.isAfter(LocalDate.now())) {
                continue;
            }

            createAssignment(task, resolveAssignee(task), dueDate);
        }
    }

    private TaskAssignmentResponseDto createAssignment(Task task, HouseholdMember member, LocalDate dueDate) {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setHouseholdMember(member);
        assignment.setDueDate(dueDate);

        return taskAssignmentMapper.toDto(taskAssignmentRepository.save(assignment));
    }

    private HouseholdMember resolveAssignee(Task task) {
        if (task.getStrategy() == AssignmentStrategy.SOLO) {
            HouseholdMember member = task.getResponsibleMember();
            if (member == null) {
                throw new IllegalStateException("Task " + task.getId() + " has SOLO strategy but no responsible member set");
            }
            return member;
        }

        List<HouseholdMember> members = householdMemberRepository.findByHouseholdOrderByCreatedAtAsc(task.getHousehold());
        if (members.isEmpty()) {
            throw new IllegalStateException("Household " + task.getHousehold().getId() + " has no members to rotate task " + task.getId());
        }

        Optional<TaskAssignment> lastAssignment = taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task);
        if (lastAssignment.isEmpty()) {
            return members.getFirst();
        }

        UUID lastMemberId = lastAssignment.get().getHouseholdMember().getId();
        int lastIndex = -1;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(lastMemberId)) {
                lastIndex = i;
                break;
            }
        }

        return members.get((lastIndex + 1) % members.size());
    }

    private LocalDate calculateTaskDueDate(Task task) {
        Frequency frequency = task.getFrequency();

        if (frequency == Frequency.AS_NEEDED) {
            return null;
        }

        if (frequency == Frequency.ONCE) {
            return task.getStartingDate();
        }

        Period period = periodFor(frequency);

        if (task.isAlwaysSameDay()) {
            LocalDate dueDate = task.getStartingDate();
            while (dueDate.isBefore(LocalDate.now())) {
                dueDate = dueDate.plus(period);
            }
            return dueDate;
        }

        return LocalDate.now().plus(period);
    }

    private Period periodFor(Frequency frequency) {
        return switch (frequency) {
            case DAILY -> Period.ofDays(1);
            case EVERY_OTHER_DAY -> Period.ofDays(2);
            case EVERY_THREE_DAYS -> Period.ofDays(3);
            case EVERY_FOUR_DAYS -> Period.ofDays(4);
            case WEEKLY -> Period.ofWeeks(1);
            case EVERY_OTHER_WEEK -> Period.ofWeeks(2);
            case MONTHLY -> Period.ofMonths(1);
            case EVERY_TWO_MONTHS -> Period.ofMonths(2);
            case EVERY_THREE_MONTHS -> Period.ofMonths(3);
            case EVERY_SIX_MONTHS -> Period.ofMonths(6);
            case YEARLY -> Period.ofYears(1);
            case ONCE, AS_NEEDED -> throw new IllegalStateException("No fixed period for frequency " + frequency);
        };
    }

    private HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Member with id " + id + " not found"));
    }

    private Task getTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Task with id " + id + " not found"));
    }

    private TaskAssignment getAssignment(UUID id) {
        return taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Assignment with id " + id + " not found"));
    }
}

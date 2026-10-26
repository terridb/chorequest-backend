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

import java.time.Clock;
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
    private final Clock clock;

    public TaskAssignmentService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository, TaskAssignmentMapper taskAssignmentMapper, HouseholdMemberRepository householdMemberRepository, HouseholdService householdService, Clock clock) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
        this.clock = clock;
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

        if (!member.isActive()) {
            throw new BadRequestException("Household member has left the household");
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

        if (!member.isActive()) {
            throw new BadRequestException("Household member has left the household");
        }

        return createAssignment(task, member, calculateTaskDueDate(task));
    }

    public TaskAssignmentResponseDto createInitialAssignment(Task task) {
        return createAssignment(task, resolveAssignee(task), calculateInitialDueDate(task));
    }

    public void rescheduleOpenAssignments(Task task) {
        List<TaskAssignment> openAssignments = taskAssignmentRepository.findByTaskAndTaskCompletionIsNull(task);
        LocalDate dueDate = calculateInitialDueDate(task);

        // A task switched from ONCE or AS_NEEDED to a recurring frequency may have nothing open yet
        if (openAssignments.isEmpty()) {
            if (isRecurring(task)) {
                createAssignment(task, resolveAssignee(task), dueDate);
            }
            return;
        }

        for (TaskAssignment assignment : openAssignments) {
            assignment.setDueDate(dueDate);
        }
        taskAssignmentRepository.saveAll(openAssignments);
    }

    // For SOLO tasks the responsible member owns every assignment; for ROTATING tasks the rotation restarts with them
    public void reassignOpenAssignmentsToResponsibleMember(Task task) {
        HouseholdMember responsibleMember = task.getResponsibleMember();
        if (responsibleMember == null) {
            return;
        }

        List<TaskAssignment> openAssignments = taskAssignmentRepository.findByTaskAndTaskCompletionIsNull(task);
        for (TaskAssignment assignment : openAssignments) {
            assignment.setHouseholdMember(responsibleMember);
        }
        taskAssignmentRepository.saveAll(openAssignments);
    }

    public void createNextAssignment(TaskAssignment completedAssignment) {
        Task task = completedAssignment.getTask();
        if (task.getFrequency() == Frequency.ONCE) {
            return;
        }

        createAssignment(task, resolveAssignee(task), calculateNextDueDate(task, completedAssignment.getDueDate()));
    }

    public void reassignOpenAssignments(HouseholdMember leavingMember) {
        List<HouseholdMember> members = householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(leavingMember.getHousehold());
        List<TaskAssignment> openAssignments = taskAssignmentRepository.findByHouseholdMemberAndTaskCompletionIsNull(leavingMember);

        if (members.size() <= 1) {
            taskAssignmentRepository.deleteAll(openAssignments);
            return;
        }

        HouseholdMember nextMember = memberAfter(members, leavingMember.getId());
        for (TaskAssignment assignment : openAssignments) {
            HouseholdMember owner = assignment.getTask().getResponsibleMember();
            boolean hasOtherSoloOwner = assignment.getTask().getStrategy() == AssignmentStrategy.SOLO
                    && owner != null && !owner.getId().equals(leavingMember.getId());
            assignment.setHouseholdMember(hasOtherSoloOwner ? owner : nextMember);
        }
        taskAssignmentRepository.saveAll(openAssignments);
    }

    private boolean isRecurring(Task task) {
        return task.getFrequency() != Frequency.AS_NEEDED && task.getFrequency() != Frequency.ONCE;
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
                throw new BadRequestException("Task " + task.getId() + " has SOLO strategy but no responsible member set");
            }
            return member;
        }

        List<HouseholdMember> members = householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(task.getHousehold());
        if (members.isEmpty()) {
            throw new BadRequestException("Household " + task.getHousehold().getId() + " has no members to rotate task " + task.getId());
        }

        Optional<TaskAssignment> lastAssignment = taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task);
        if (lastAssignment.isEmpty()) {
            HouseholdMember startingMember = task.getResponsibleMember();
            return startingMember != null ? startingMember : members.getFirst();
        }

        return memberAfter(members, lastAssignment.get().getHouseholdMember().getId());
    }

    private HouseholdMember memberAfter(List<HouseholdMember> members, UUID memberId) {
        int index = -1;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(memberId)) {
                index = i;
                break;
            }
        }

        return members.get((index + 1) % members.size());
    }

    private LocalDate calculateInitialDueDate(Task task) {
        if (!isRecurring(task)) {
            return calculateTaskDueDate(task);
        }

        LocalDate today = today(task);
        if (!task.getStartingDate().isBefore(today)) {
            return task.getStartingDate();
        }

        return task.isAlwaysSameDay() ? calculateTaskDueDate(task) : today;
    }

    private LocalDate calculateNextDueDate(Task task, LocalDate completedDueDate) {
        LocalDate dueDate = calculateTaskDueDate(task);
        if (dueDate == null || !task.isAlwaysSameDay() || completedDueDate == null) {
            return dueDate;
        }

        Period period = periodFor(task.getFrequency());
        while (!dueDate.isAfter(completedDueDate)) {
            dueDate = dueDate.plus(period);
        }
        return dueDate;
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
            LocalDate today = today(task);
            LocalDate dueDate = task.getStartingDate();
            while (dueDate.isBefore(today)) {
                dueDate = dueDate.plus(period);
            }
            return dueDate;
        }

        return today(task).plus(period);
    }

    private LocalDate today(Task task) {
        return LocalDate.now(clock.withZone(task.getHousehold().getZoneId()));
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

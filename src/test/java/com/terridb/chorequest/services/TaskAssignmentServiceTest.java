package com.terridb.chorequest.services;

import com.terridb.chorequest.entities.BaseEntity;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.Frequency;
import com.terridb.chorequest.mappers.TaskAssignmentMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private TaskAssignmentMapper taskAssignmentMapper;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private HouseholdService householdService;

    // 22:30 UTC on Oct 12 is already 00:30 on Oct 13 in Amsterdam
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-12T22:30:00Z"), ZoneOffset.UTC);

    private TaskAssignmentService taskAssignmentService;

    @BeforeEach
    void setUp() {
        taskAssignmentService = new TaskAssignmentService(taskRepository, taskAssignmentRepository, taskAssignmentMapper, householdMemberRepository, householdService, clock);
    }

    @Test
    void reassignOpenAssignments_givesRotatingAssignmentToNextMemberInRotation() {
        Household household = new Household();
        HouseholdMember first = member(household);
        HouseholdMember leaving = member(household);
        HouseholdMember last = member(household);
        TaskAssignment assignment = openAssignment(task(AssignmentStrategy.ROTATING, null), leaving);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, leaving, last));
        when(taskAssignmentRepository.findByHouseholdMemberAndTaskCompletionIsNull(leaving)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        assertThat(assignment.getHouseholdMember()).isEqualTo(last);
        verify(taskAssignmentRepository).saveAll(List.of(assignment));
    }

    @Test
    void reassignOpenAssignments_wrapsAroundToFirstMember_whenLeavingMemberIsLastInRotation() {
        Household household = new Household();
        HouseholdMember first = member(household);
        HouseholdMember leaving = member(household);
        TaskAssignment assignment = openAssignment(task(AssignmentStrategy.ROTATING, null), leaving);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, leaving));
        when(taskAssignmentRepository.findByHouseholdMemberAndTaskCompletionIsNull(leaving)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        assertThat(assignment.getHouseholdMember()).isEqualTo(first);
    }

    @Test
    void reassignOpenAssignments_givesSoloAssignmentBackToItsOwner() {
        Household household = new Household();
        HouseholdMember owner = member(household);
        HouseholdMember leaving = member(household);
        HouseholdMember last = member(household);
        TaskAssignment assignment = openAssignment(task(AssignmentStrategy.SOLO, owner), leaving);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(owner, leaving, last));
        when(taskAssignmentRepository.findByHouseholdMemberAndTaskCompletionIsNull(leaving)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        assertThat(assignment.getHouseholdMember()).isEqualTo(owner);
    }

    @Test
    void reassignOpenAssignments_deletesOpenAssignments_whenNobodyElseIsLeft() {
        Household household = new Household();
        HouseholdMember leaving = member(household);
        TaskAssignment assignment = openAssignment(task(AssignmentStrategy.ROTATING, null), leaving);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(leaving));
        when(taskAssignmentRepository.findByHouseholdMemberAndTaskCompletionIsNull(leaving)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        verify(taskAssignmentRepository).deleteAll(List.of(assignment));
        verify(taskAssignmentRepository, never()).saveAll(anyList());
    }

    @Test
    void generateDueAssignments_usesHouseholdLocalDate_whenItIsAlreadyTomorrowThere() {
        Task task = dailySoloTaskStarting(LocalDate.of(2026, 10, 13), "Europe/Amsterdam");

        when(taskRepository.findByFrequencyNotIn(anyList())).thenReturn(List.of(task));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.generateDueAssignments();

        ArgumentCaptor<TaskAssignment> saved = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(taskAssignmentRepository).save(saved.capture());
        assertThat(saved.getValue().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 13));
    }

    @Test
    void generateDueAssignments_skipsTask_whenItIsNotYetDueInHouseholdLocalDate() {
        Task task = dailySoloTaskStarting(LocalDate.of(2026, 10, 13), "UTC");

        when(taskRepository.findByFrequencyNotIn(anyList())).thenReturn(List.of(task));

        taskAssignmentService.generateDueAssignments();

        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    private Task dailySoloTaskStarting(LocalDate startingDate, String timeZone) {
        Household household = new Household();
        household.setTimeZone(timeZone);
        Task task = task(AssignmentStrategy.SOLO, member(household));
        task.setHousehold(household);
        task.setFrequency(Frequency.DAILY);
        task.setAlwaysSameDay(true);
        task.setStartingDate(startingDate);
        return task;
    }

    private HouseholdMember member(Household household) {
        HouseholdMember member = new HouseholdMember();
        setId(member, UUID.randomUUID());
        member.setHousehold(household);
        return member;
    }

    private Task task(AssignmentStrategy strategy, HouseholdMember responsibleMember) {
        Task task = new Task();
        task.setStrategy(strategy);
        task.setResponsibleMember(responsibleMember);
        return task;
    }

    private TaskAssignment openAssignment(Task task, HouseholdMember member) {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setHouseholdMember(member);
        return assignment;
    }

    private void setId(BaseEntity entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}

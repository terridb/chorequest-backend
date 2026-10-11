package com.terridb.chorequest.services;

import com.terridb.chorequest.entities.BaseEntity;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.mappers.TaskAssignmentMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    private TaskAssignmentService taskAssignmentService;

    @BeforeEach
    void setUp() {
        taskAssignmentService = new TaskAssignmentService(taskRepository, taskAssignmentRepository, taskAssignmentMapper, householdMemberRepository, householdService);
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

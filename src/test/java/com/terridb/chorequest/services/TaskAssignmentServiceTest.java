package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentRequestDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskAssignmentResponseDto;
import com.terridb.chorequest.dtos.taskAssignments.TaskHistoryEntryDto;
import com.terridb.chorequest.entities.BaseEntity;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.enums.AssignmentStatus;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.Frequency;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.TaskAssignmentMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        when(taskAssignmentRepository.findByHouseholdMemberAndStatus(leaving, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

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
        when(taskAssignmentRepository.findByHouseholdMemberAndStatus(leaving, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

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
        when(taskAssignmentRepository.findByHouseholdMemberAndStatus(leaving, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        assertThat(assignment.getHouseholdMember()).isEqualTo(owner);
    }

    @Test
    void reassignOpenAssignments_deletesOpenAssignments_whenNobodyElseIsLeft() {
        Household household = new Household();
        HouseholdMember leaving = member(household);
        TaskAssignment assignment = openAssignment(task(AssignmentStrategy.ROTATING, null), leaving);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(leaving));
        when(taskAssignmentRepository.findByHouseholdMemberAndStatus(leaving, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignments(leaving);

        verify(taskAssignmentRepository).deleteAll(List.of(assignment));
        verify(taskAssignmentRepository, never()).saveAll(anyList());
    }

    @Test
    void generateDueAssignments_usesHouseholdLocalDate_whenItIsAlreadyTomorrowThere() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 13), "Europe/Amsterdam");

        when(taskRepository.findByFrequencyNotIn(anyList())).thenReturn(List.of(task));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.generateDueAssignments();

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 13));
    }

    @Test
    void generateDueAssignments_skipsTask_whenItIsNotYetDueInHouseholdLocalDate() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 13), "UTC");

        when(taskRepository.findByFrequencyNotIn(anyList())).thenReturn(List.of(task));

        taskAssignmentService.generateDueAssignments();

        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void createNextAssignment_schedulesNextWeek_whenSameDayTaskIsCompletedOnItsDueDate() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 12), "UTC");
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        completed.setDueDate(LocalDate.of(2026, 10, 12));

        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 19));
    }

    @Test
    void createNextAssignment_skipsCompletedDueDate_whenSameDayTaskIsCompletedEarly() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        completed.setDueDate(LocalDate.of(2026, 10, 19));

        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 26));
    }

    @Test
    void createNextAssignment_schedulesThisWeek_whenSameDayTaskIsCompletedLate() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 9, 28), "UTC");
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        completed.setDueDate(LocalDate.of(2026, 10, 5));

        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void createNextAssignment_countsFromHouseholdLocalDate_whenItIsAlreadyTomorrowThere() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "Europe/Amsterdam");
        task.setAlwaysSameDay(false);
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        completed.setDueDate(LocalDate.of(2026, 10, 12));

        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    void createNextAssignment_createsUndatedAssignment_whenAsNeededTaskIsCompleted() {
        Task task = soloTask(Frequency.AS_NEEDED, LocalDate.of(2026, 10, 12), "UTC");
        task.setAlwaysSameDay(false);
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());

        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isNull();
        assertThat(saved.getHouseholdMember()).isEqualTo(task.getResponsibleMember());
    }

    @Test
    void createNextAssignment_createsNothing_whenOnceTaskIsCompleted() {
        Task task = soloTask(Frequency.ONCE, LocalDate.of(2026, 10, 12), "UTC");
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        completed.setDueDate(LocalDate.of(2026, 10, 12));

        taskAssignmentService.createNextAssignment(completed);

        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void rescheduleOpenAssignments_movesOpenAssignmentToNewStartingDate() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 20), "UTC");
        TaskAssignment assignment = openAssignment(task, task.getResponsibleMember());
        assignment.setDueDate(LocalDate.of(2026, 10, 12));

        when(taskAssignmentRepository.findByTaskAndStatus(task, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

        taskAssignmentService.rescheduleOpenAssignments(task);

        assertThat(assignment.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        verify(taskAssignmentRepository).saveAll(List.of(assignment));
    }

    @Test
    void rescheduleOpenAssignments_createsAssignment_whenRecurringTaskHasNoneOpen() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 20), "UTC");

        when(taskAssignmentRepository.findByTaskAndStatus(task, AssignmentStatus.OPEN)).thenReturn(List.of());
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskAssignmentService.rescheduleOpenAssignments(task);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(saved.getHouseholdMember()).isEqualTo(task.getResponsibleMember());
    }

    @Test
    void rescheduleOpenAssignments_createsNothing_whenNonRecurringTaskHasNoneOpen() {
        Task task = soloTask(Frequency.ONCE, LocalDate.of(2026, 10, 20), "UTC");

        when(taskAssignmentRepository.findByTaskAndStatus(task, AssignmentStatus.OPEN)).thenReturn(List.of());

        taskAssignmentService.rescheduleOpenAssignments(task);

        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void reassignOpenAssignmentsToResponsibleMember_movesOpenAssignmentToNewResponsibleMember() {
        Household household = new Household();
        HouseholdMember previous = member(household);
        HouseholdMember newOwner = member(household);
        Task task = task(AssignmentStrategy.SOLO, newOwner);
        TaskAssignment assignment = openAssignment(task, previous);

        when(taskAssignmentRepository.findByTaskAndStatus(task, AssignmentStatus.OPEN)).thenReturn(List.of(assignment));

        taskAssignmentService.reassignOpenAssignmentsToResponsibleMember(task);

        assertThat(assignment.getHouseholdMember()).isEqualTo(newOwner);
        verify(taskAssignmentRepository).saveAll(List.of(assignment));
    }

    @Test
    void reassignOpenAssignmentsToResponsibleMember_keepsAssignee_whenNoResponsibleMember() {
        Household household = new Household();
        HouseholdMember current = member(household);
        Task task = task(AssignmentStrategy.ROTATING, null);
        TaskAssignment assignment = openAssignment(task, current);

        taskAssignmentService.reassignOpenAssignmentsToResponsibleMember(task);

        assertThat(assignment.getHouseholdMember()).isEqualTo(current);
        verify(taskAssignmentRepository, never()).saveAll(anyList());
    }

    @Test
    void getAssignmentById_returnsAssignment() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 12), "UTC");
        TaskAssignment assignment = openAssignment(task, task.getResponsibleMember());
        UUID id = UUID.randomUUID();
        setId(assignment, id);
        TaskAssignmentResponseDto dto = new TaskAssignmentResponseDto();

        when(taskAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
        when(taskAssignmentMapper.toDto(assignment)).thenReturn(dto);

        TaskAssignmentResponseDto result = taskAssignmentService.getAssignmentById(id, "sub");

        assertThat(result).isSameAs(dto);
        verify(householdService).requireMembership(task.getHousehold(), "sub");
    }

    @Test
    void getAssignmentsForTask_returnsAssignmentsOfTaskInRepositoryOrder() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        UUID taskId = UUID.randomUUID();
        TaskAssignment newest = openAssignment(task, task.getResponsibleMember());
        TaskAssignment oldest = openAssignment(task, task.getResponsibleMember());
        List<TaskAssignmentResponseDto> dtos = List.of(new TaskAssignmentResponseDto(), new TaskAssignmentResponseDto());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByTaskOrderByDueDateDesc(task)).thenReturn(new LinkedHashSet<>(List.of(newest, oldest)));
        when(taskAssignmentMapper.toDto(List.of(newest, oldest))).thenReturn(dtos);

        List<TaskAssignmentResponseDto> result = taskAssignmentService.getAssignmentsForTask(taskId, "sub");

        assertThat(result).isSameAs(dtos);
        verify(householdService).requireMembership(task.getHousehold(), "sub");
    }

    @Test
    void getAssignmentsForTask_throwsRecordNotFound_whenTaskDoesNotExist() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.getAssignmentsForTask(taskId, "sub"))
                .isInstanceOf(RecordNotFoundException.class);
        verify(taskAssignmentRepository, never()).findByTaskOrderByDueDateDesc(any());
    }

    @Test
    void getHistoryForTask_returnsHistoryLimitedToRequestedAmount() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        UUID taskId = UUID.randomUUID();
        TaskAssignment completed = openAssignment(task, task.getResponsibleMember());
        List<TaskHistoryEntryDto> dtos = List.of(new TaskHistoryEntryDto());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findHistoryByTask(task, Limit.of(5))).thenReturn(List.of(completed));
        when(taskAssignmentMapper.toHistoryDto(List.of(completed))).thenReturn(dtos);

        List<TaskHistoryEntryDto> result = taskAssignmentService.getHistoryForTask(taskId, 5, "sub");

        assertThat(result).isSameAs(dtos);
        verify(householdService).requireMembership(task.getHousehold(), "sub");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 51})
    void getHistoryForTask_rejectsLimitOutsideOneToFifty(int limit) {
        UUID taskId = UUID.randomUUID();

        assertThatThrownBy(() -> taskAssignmentService.getHistoryForTask(taskId, limit, "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Limit must be between 1 and 50");
        verify(taskRepository, never()).findById(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50})
    void getHistoryForTask_acceptsLimitOnTheBoundaries(int limit) {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskAssignmentService.getHistoryForTask(taskId, limit, "sub");

        verify(taskAssignmentRepository).findHistoryByTask(task, Limit.of(limit));
    }

    @Test
    void getHistoryForTask_doesNotLoadHistory_whenUserIsNotAMember() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(householdService.requireMembership(task.getHousehold(), "sub")).thenThrow(new AccessDeniedException());

        assertThatThrownBy(() -> taskAssignmentService.getHistoryForTask(taskId, 10, "sub"))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskAssignmentRepository, never()).findHistoryByTask(any(), any());
    }

    @Test
    void getAssignmentById_throwsRecordNotFound_whenAssignmentDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(taskAssignmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.getAssignmentById(id, "sub"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Assignment with id " + id + " not found");
        verifyNoInteractions(householdService, taskAssignmentMapper);
    }

    @Test
    void getAssignmentsForMember_returnsAssignmentsOfMemberInRepositoryOrder() {
        HouseholdMember member = member(household("UTC"));
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment newest = openAssignment(task, member);
        TaskAssignment oldest = openAssignment(task, member);
        List<TaskAssignmentResponseDto> dtos = List.of(new TaskAssignmentResponseDto(), new TaskAssignmentResponseDto());

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(taskAssignmentRepository.findByHouseholdMemberOrderByDueDateDesc(member)).thenReturn(new LinkedHashSet<>(List.of(newest, oldest)));
        when(taskAssignmentMapper.toDto(List.of(newest, oldest))).thenReturn(dtos);

        List<TaskAssignmentResponseDto> result = taskAssignmentService.getAssignmentsForMember(member.getId(), "sub");

        assertThat(result).isSameAs(dtos);
        verify(householdService).requireMembership(member.getHousehold(), "sub");
    }

    @Test
    void getAssignmentsForMember_throwsRecordNotFound_whenMemberDoesNotExist() {
        UUID memberId = UUID.randomUUID();

        when(householdMemberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAssignmentService.getAssignmentsForMember(memberId, "sub"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Member with id " + memberId + " not found");
        verifyNoInteractions(householdService, taskAssignmentRepository);
    }

    @Test
    void getOpenAssignmentsForHousehold_returnsOpenAssignmentsInRepositoryOrder() {
        Household household = household("UTC");
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment dueFirst = openAssignment(task, task.getResponsibleMember());
        TaskAssignment dueLast = openAssignment(task, task.getResponsibleMember());
        List<TaskAssignmentResponseDto> dtos = List.of(new TaskAssignmentResponseDto(), new TaskAssignmentResponseDto());

        when(householdService.getHousehold(household.getId())).thenReturn(household);
        when(taskAssignmentRepository.findByTask_HouseholdAndStatusOrderByDueDateAsc(household, AssignmentStatus.OPEN))
                .thenReturn(new LinkedHashSet<>(List.of(dueFirst, dueLast)));
        when(taskAssignmentMapper.toDto(List.of(dueFirst, dueLast))).thenReturn(dtos);

        List<TaskAssignmentResponseDto> result = taskAssignmentService.getOpenAssignmentsForHousehold(household.getId(), "sub");

        assertThat(result).isSameAs(dtos);
        verify(householdService).requireMembership(household, "sub");
    }

    @Test
    void updateAssignment_reassignsOpenAssignmentToOtherActiveMember() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        HouseholdMember newMember = member(task.getHousehold());
        TaskAssignmentResponseDto dto = new TaskAssignmentResponseDto();

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdMemberRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
        when(taskAssignmentRepository.save(assignment)).thenReturn(assignment);
        when(taskAssignmentMapper.toDto(assignment)).thenReturn(dto);

        TaskAssignmentResponseDto result = taskAssignmentService.updateAssignment(assignment.getId(), requestFor(newMember), "sub");

        assertThat(result).isSameAs(dto);
        assertThat(assignment.getHouseholdMember()).isEqualTo(newMember);
        verify(householdService).requireMembership(task.getHousehold(), "sub");
    }

    @ParameterizedTest
    @EnumSource(value = AssignmentStatus.class, names = {"COMPLETED", "SKIPPED"})
    void updateAssignment_rejectsAssignmentThatIsNoLongerOpen(AssignmentStatus status) {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        HouseholdMember originalMember = task.getResponsibleMember();
        TaskAssignment assignment = storedAssignment(task, originalMember);
        close(assignment, status);
        HouseholdMember newMember = member(task.getHousehold());

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdMemberRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));

        assertThatThrownBy(() -> taskAssignmentService.updateAssignment(assignment.getId(), requestFor(newMember), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(notOpenMessage(status));
        assertThat(assignment.getHouseholdMember()).isEqualTo(originalMember);
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void updateAssignment_rejectsMemberOfOtherHousehold() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        HouseholdMember originalMember = task.getResponsibleMember();
        TaskAssignment assignment = storedAssignment(task, originalMember);
        HouseholdMember outsider = member(household("UTC"));

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdMemberRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> taskAssignmentService.updateAssignment(assignment.getId(), requestFor(outsider), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Household member does not belong to this task's household");
        assertThat(assignment.getHouseholdMember()).isEqualTo(originalMember);
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void updateAssignment_rejectsMemberWhoLeftTheHousehold() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        HouseholdMember originalMember = task.getResponsibleMember();
        TaskAssignment assignment = storedAssignment(task, originalMember);
        HouseholdMember formerMember = member(task.getHousehold());
        formerMember.setLeftAt(LocalDateTime.of(2026, 10, 1, 12, 0));

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdMemberRepository.findById(formerMember.getId())).thenReturn(Optional.of(formerMember));

        assertThatThrownBy(() -> taskAssignmentService.updateAssignment(assignment.getId(), requestFor(formerMember), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Household member has left the household");
        assertThat(assignment.getHouseholdMember()).isEqualTo(originalMember);
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void deleteAssignment_deletesOpenAssignment() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        taskAssignmentService.deleteAssignment(assignment.getId(), "sub");

        verify(householdService).requireMembership(task.getHousehold(), "sub");
        verify(taskAssignmentRepository).delete(assignment);
    }

    @ParameterizedTest
    @EnumSource(value = AssignmentStatus.class, names = {"COMPLETED", "SKIPPED"})
    void deleteAssignment_rejectsAssignmentThatIsNoLongerOpen(AssignmentStatus status) {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        close(assignment, status);

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> taskAssignmentService.deleteAssignment(assignment.getId(), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(notOpenMessage(status));
        verify(taskAssignmentRepository, never()).delete(any(TaskAssignment.class));
    }

    @Test
    void deleteAssignment_doesNotDelete_whenUserIsNotAMember() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdService.requireMembership(task.getHousehold(), "sub")).thenThrow(new AccessDeniedException());

        assertThatThrownBy(() -> taskAssignmentService.deleteAssignment(assignment.getId(), "sub"))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskAssignmentRepository, never()).delete(any(TaskAssignment.class));
    }

    @Test
    void assignTaskToMember_createsAssignmentForMemberDueOnePeriodFromToday() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        task.setAlwaysSameDay(false);
        HouseholdMember member = member(task.getHousehold());
        TaskAssignmentResponseDto dto = new TaskAssignmentResponseDto();

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskAssignmentMapper.toDto(any(TaskAssignment.class))).thenReturn(dto);

        TaskAssignmentResponseDto result = taskAssignmentService.assignTaskToMember(task.getId(), member.getId(), "sub");

        TaskAssignment saved = savedAssignment();
        assertThat(result).isSameAs(dto);
        assertThat(saved.getTask()).isEqualTo(task);
        assertThat(saved.getHouseholdMember()).isEqualTo(member);
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 19));
        assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.OPEN);
        verify(householdService).requireMembership(task.getHousehold(), "sub");
    }

    @Test
    void assignTaskToMember_rejectsMemberOfOtherHousehold() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        HouseholdMember outsider = member(household("UTC"));

        when(householdMemberRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskAssignmentService.assignTaskToMember(task.getId(), outsider.getId(), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Household member does not belong to this task's household");
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void assignTaskToMember_rejectsMemberWhoLeftTheHousehold() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        HouseholdMember formerMember = member(task.getHousehold());
        formerMember.setLeftAt(LocalDateTime.of(2026, 10, 1, 12, 0));

        when(householdMemberRepository.findById(formerMember.getId())).thenReturn(Optional.of(formerMember));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskAssignmentService.assignTaskToMember(task.getId(), formerMember.getId(), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Household member has left the household");
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void createInitialAssignment_usesStartingDate_whenItIsInTheFuture() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 20), "UTC");
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(saved.getHouseholdMember()).isEqualTo(task.getResponsibleMember());
    }

    @Test
    void createInitialAssignment_usesStartingDate_whenItIsToday() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 12), "UTC");
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void createInitialAssignment_usesNextOccurrenceOnSameWeekday_whenSameDayTaskStartedInThePast() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 1), "UTC");
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void createInitialAssignment_isDueToday_whenFlexibleTaskStartedInThePast() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 1), "UTC");
        task.setAlwaysSameDay(false);
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void createInitialAssignment_usesStartingDate_whenOnceTaskStartedInThePast() {
        Task task = soloTask(Frequency.ONCE, LocalDate.of(2026, 10, 1), "UTC");
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void createInitialAssignment_createsUndatedAssignment_whenTaskIsAsNeeded() {
        Task task = soloTask(Frequency.AS_NEEDED, LocalDate.of(2026, 10, 1), "UTC");
        task.setAlwaysSameDay(false);
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getDueDate()).isNull();
    }

    @Test
    void createInitialAssignment_givesRotatingTaskToResponsibleMember_whenItHasNoAssignmentsYet() {
        Household household = household("UTC");
        HouseholdMember first = member(household);
        HouseholdMember responsible = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, responsible);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, responsible));
        when(taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task)).thenReturn(Optional.empty());
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getHouseholdMember()).isEqualTo(responsible);
    }

    @Test
    void createInitialAssignment_givesRotatingTaskToFirstMember_whenItHasNoAssignmentsOrResponsibleMember() {
        Household household = household("UTC");
        HouseholdMember first = member(household);
        HouseholdMember second = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, null);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, second));
        when(taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task)).thenReturn(Optional.empty());
        stubSaveReturnsArgument();

        taskAssignmentService.createInitialAssignment(task);

        assertThat(savedAssignment().getHouseholdMember()).isEqualTo(first);
    }

    @Test
    void createInitialAssignment_rejectsSoloTaskWithoutResponsibleMember() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        task.setResponsibleMember(null);

        assertThatThrownBy(() -> taskAssignmentService.createInitialAssignment(task))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Task " + task.getId() + " has SOLO strategy but no responsible member set");
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void createInitialAssignment_rejectsRotatingTask_whenHouseholdHasNoActiveMembers() {
        Household household = household("UTC");
        Task task = rotatingTask(Frequency.WEEKLY, household, null);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of());

        assertThatThrownBy(() -> taskAssignmentService.createInitialAssignment(task))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Household " + household.getId() + " has no members to rotate task " + task.getId());
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void createNextAssignment_givesRotatingTaskToNextMember() {
        Household household = household("UTC");
        HouseholdMember first = member(household);
        HouseholdMember second = member(household);
        HouseholdMember third = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, null);
        TaskAssignment completed = openAssignment(task, first);
        completed.setDueDate(LocalDate.of(2026, 10, 12));

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, second, third));
        when(taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task)).thenReturn(Optional.of(completed));
        stubSaveReturnsArgument();

        taskAssignmentService.createNextAssignment(completed);

        TaskAssignment saved = savedAssignment();
        assertThat(saved.getHouseholdMember()).isEqualTo(second);
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 19));
    }

    @Test
    void createNextAssignment_wrapsRotatingTaskAroundToFirstMember_afterLastMember() {
        Household household = household("UTC");
        HouseholdMember first = member(household);
        HouseholdMember last = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, null);
        TaskAssignment completed = openAssignment(task, last);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, last));
        when(taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task)).thenReturn(Optional.of(completed));
        stubSaveReturnsArgument();

        taskAssignmentService.createNextAssignment(completed);

        assertThat(savedAssignment().getHouseholdMember()).isEqualTo(first);
    }

    @Test
    void createNextAssignment_givesRotatingTaskToFirstMember_whenLastAssigneeHasLeft() {
        Household household = household("UTC");
        HouseholdMember first = member(household);
        HouseholdMember second = member(household);
        HouseholdMember formerMember = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, null);
        TaskAssignment completed = openAssignment(task, formerMember);

        when(householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household)).thenReturn(List.of(first, second));
        when(taskAssignmentRepository.findFirstByTaskOrderByCreatedAtDesc(task)).thenReturn(Optional.of(completed));
        stubSaveReturnsArgument();

        taskAssignmentService.createNextAssignment(completed);

        assertThat(savedAssignment().getHouseholdMember()).isEqualTo(first);
    }

    @Test
    void generateDueAssignments_skipsTask_whenItAlreadyHasAnOpenAssignment() {
        Task task = soloTask(Frequency.DAILY, LocalDate.of(2026, 10, 1), "UTC");

        when(taskRepository.findByFrequencyNotIn(List.of(Frequency.AS_NEEDED, Frequency.ONCE))).thenReturn(List.of(task));
        when(taskAssignmentRepository.existsByTaskAndStatus(task, AssignmentStatus.OPEN)).thenReturn(true);

        taskAssignmentService.generateDueAssignments();

        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void skipAssignment_marksAssignmentSkippedAndKeepsSameMemberForNextOccurrence() {
        Household household = household("UTC");
        HouseholdMember assignee = member(household);
        HouseholdMember skipper = member(household);
        Task task = rotatingTask(Frequency.WEEKLY, household, null);
        TaskAssignment assignment = storedAssignment(task, assignee);
        assignment.setDueDate(LocalDate.of(2026, 10, 15));
        TaskAssignmentResponseDto dto = new TaskAssignmentResponseDto();

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdService.requireMembership(household, "sub")).thenReturn(skipper);
        stubSaveReturnsArgument();
        when(taskAssignmentMapper.toDto(any(TaskAssignment.class))).thenReturn(dto);

        TaskAssignmentResponseDto result = taskAssignmentService.skipAssignment(assignment.getId(), "sub");

        List<TaskAssignment> saved = savedAssignments(2);
        assertThat(saved.get(0)).isSameAs(assignment);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.SKIPPED);
        assertThat(assignment.getSkippedBy()).isEqualTo(skipper);
        assertThat(assignment.getSkippedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 22, 30));

        TaskAssignment next = saved.get(1);
        assertThat(next).isNotSameAs(assignment);
        assertThat(next.getTask()).isEqualTo(task);
        assertThat(next.getHouseholdMember()).isEqualTo(assignee);
        assertThat(next.getStatus()).isEqualTo(AssignmentStatus.OPEN);
        verify(taskAssignmentMapper).toDto(next);
        assertThat(result).isSameAs(dto);
        verifyNoInteractions(householdMemberRepository);
    }

    @Test
    void skipAssignment_schedulesNextOccurrenceOnePeriodAfterSkippedDueDate() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 1), "UTC");
        task.setAlwaysSameDay(false);
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        assignment.setDueDate(LocalDate.of(2026, 10, 15));

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        stubSaveReturnsArgument();

        taskAssignmentService.skipAssignment(assignment.getId(), "sub");

        assertThat(savedAssignments(2).get(1).getDueDate()).isEqualTo(LocalDate.of(2026, 10, 22));
    }

    @Test
    void skipAssignment_rollsNextOccurrenceForwardToToday_whenSkippedAssignmentIsOverdue() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 9, 21), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        assignment.setDueDate(LocalDate.of(2026, 9, 28));

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        stubSaveReturnsArgument();

        taskAssignmentService.skipAssignment(assignment.getId(), "sub");

        assertThat(savedAssignments(2).get(1).getDueDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void skipAssignment_rollsNextOccurrenceForwardFromHouseholdLocalDate_whenItIsAlreadyTomorrowThere() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 9, 21), "Europe/Amsterdam");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        assignment.setDueDate(LocalDate.of(2026, 9, 28));

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        stubSaveReturnsArgument();

        taskAssignmentService.skipAssignment(assignment.getId(), "sub");

        assertThat(savedAssignments(2).get(1).getDueDate()).isEqualTo(LocalDate.of(2026, 10, 19));
    }

    @ParameterizedTest
    @EnumSource(value = Frequency.class, names = {"ONCE", "AS_NEEDED"})
    void skipAssignment_rejectsTaskThatIsNotRecurring(Frequency frequency) {
        Task task = soloTask(frequency, LocalDate.of(2026, 10, 12), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> taskAssignmentService.skipAssignment(assignment.getId(), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Task is not recurring and can't be skipped");
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.OPEN);
        assertThat(assignment.getSkippedAt()).isNull();
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @ParameterizedTest
    @EnumSource(value = AssignmentStatus.class, names = {"COMPLETED", "SKIPPED"})
    void skipAssignment_rejectsAssignmentThatIsNoLongerOpen(AssignmentStatus status) {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());
        close(assignment, status);

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> taskAssignmentService.skipAssignment(assignment.getId(), "sub"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(notOpenMessage(status));
        assertThat(assignment.getStatus()).isEqualTo(status);
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    @Test
    void skipAssignment_changesNothing_whenUserIsNotAMember() {
        Task task = soloTask(Frequency.WEEKLY, LocalDate.of(2026, 10, 5), "UTC");
        TaskAssignment assignment = storedAssignment(task, task.getResponsibleMember());

        when(taskAssignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(householdService.requireMembership(task.getHousehold(), "sub")).thenThrow(new AccessDeniedException());

        assertThatThrownBy(() -> taskAssignmentService.skipAssignment(assignment.getId(), "sub"))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.OPEN);
        verify(taskAssignmentRepository, never()).save(any(TaskAssignment.class));
    }

    private Task soloTask(Frequency frequency, LocalDate startingDate, String timeZone) {
        Household household = household(timeZone);
        Task task = task(AssignmentStrategy.SOLO, member(household));
        setId(task, UUID.randomUUID());
        task.setHousehold(household);
        task.setFrequency(frequency);
        task.setAlwaysSameDay(true);
        task.setStartingDate(startingDate);
        return task;
    }

    private Task rotatingTask(Frequency frequency, Household household, HouseholdMember responsibleMember) {
        Task task = task(AssignmentStrategy.ROTATING, responsibleMember);
        setId(task, UUID.randomUUID());
        task.setHousehold(household);
        task.setFrequency(frequency);
        task.setAlwaysSameDay(false);
        task.setStartingDate(LocalDate.of(2026, 10, 5));
        return task;
    }

    private Household household(String timeZone) {
        Household household = new Household();
        setId(household, UUID.randomUUID());
        household.setTimeZone(timeZone);
        return household;
    }

    private TaskAssignment storedAssignment(Task task, HouseholdMember member) {
        TaskAssignment assignment = openAssignment(task, member);
        setId(assignment, UUID.randomUUID());
        return assignment;
    }

    private void close(TaskAssignment assignment, AssignmentStatus status) {
        switch (status) {
            case COMPLETED -> assignment.complete();
            case SKIPPED -> assignment.skip(assignment.getHouseholdMember(), LocalDateTime.of(2026, 10, 10, 9, 0));
            case OPEN -> throw new IllegalArgumentException("An open assignment can't be closed");
        }
    }

    private String notOpenMessage(AssignmentStatus status) {
        return switch (status) {
            case COMPLETED -> "Assignment has already been completed";
            case SKIPPED -> "Assignment has already been skipped";
            case OPEN -> throw new IllegalArgumentException("An open assignment has no rejection message");
        };
    }

    private TaskAssignmentRequestDto requestFor(HouseholdMember member) {
        TaskAssignmentRequestDto request = new TaskAssignmentRequestDto();
        request.setHouseholdMemberId(member.getId());
        return request;
    }

    private void stubSaveReturnsArgument() {
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private List<TaskAssignment> savedAssignments(int expectedSaves) {
        ArgumentCaptor<TaskAssignment> saved = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(taskAssignmentRepository, times(expectedSaves)).save(saved.capture());
        return saved.getAllValues();
    }

    private TaskAssignment savedAssignment() {
        ArgumentCaptor<TaskAssignment> saved = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(taskAssignmentRepository).save(saved.capture());
        return saved.getValue();
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

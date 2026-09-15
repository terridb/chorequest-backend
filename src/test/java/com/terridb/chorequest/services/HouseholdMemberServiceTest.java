package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.BaseEntity;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.mappers.UserMapper;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdMemberServiceTest {

    private static final String SUB = "auth0|caller";

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private HouseholdService householdService;

    @Mock
    private UserService userService;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private TaskRepository taskRepository;

    private HouseholdMemberService householdMemberService;

    @BeforeEach
    void setUp() {
        HouseholdMemberMapper householdMemberMapper = new HouseholdMemberMapper(new UserMapper());
        householdMemberService = new HouseholdMemberService(householdMemberRepository, householdMemberMapper, householdService, userService, taskAssignmentRepository, taskRepository);
    }

    @Test
    void deleteMember_allowsSelfLeave_forRegularMember() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.MEMBER);
        User caller = userWithSub(SUB);
        member.setUser(caller);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(member));

        householdMemberService.deleteMember(member.getId(), SUB);

        verify(householdMemberRepository).delete(member);
        verify(householdMemberRepository, never()).save(any());
    }

    @Test
    void deleteMember_allowsSelfLeave_forAdmin_whenAnotherAdminExists() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.ADMIN);
        User caller = userWithSub(SUB);
        member.setUser(caller);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(member));
        when(householdMemberRepository.existsByHouseholdAndRoleAndIdNot(household, HouseholdMemberRole.ADMIN, member.getId())).thenReturn(true);

        householdMemberService.deleteMember(member.getId(), SUB);

        verify(householdMemberRepository).delete(member);
        verify(householdMemberRepository, never()).save(any());
    }

    @Test
    void deleteMember_lastAdminSelfLeave_promotesLongestStandingRemainingMember() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.ADMIN);
        User caller = userWithSub(SUB);
        member.setUser(caller);
        HouseholdMember successor = memberWithRole(household, HouseholdMemberRole.MEMBER);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(member));
        when(householdMemberRepository.existsByHouseholdAndRoleAndIdNot(household, HouseholdMemberRole.ADMIN, member.getId())).thenReturn(false);
        when(householdMemberRepository.findFirstByHouseholdAndIdNotOrderByCreatedAtAsc(household, member.getId())).thenReturn(Optional.of(successor));

        householdMemberService.deleteMember(member.getId(), SUB);

        ArgumentCaptor<HouseholdMember> savedCaptor = ArgumentCaptor.forClass(HouseholdMember.class);
        verify(householdMemberRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).isEqualTo(successor);
        assertThat(savedCaptor.getValue().getRole()).isEqualTo(HouseholdMemberRole.ADMIN);
        verify(householdMemberRepository).delete(member);
    }

    @Test
    void deleteMember_lastAdminSelfLeave_withNoOtherMembers_deletesWithoutPromoting() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.ADMIN);
        User caller = userWithSub(SUB);
        member.setUser(caller);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(member));
        when(householdMemberRepository.existsByHouseholdAndRoleAndIdNot(household, HouseholdMemberRole.ADMIN, member.getId())).thenReturn(false);
        when(householdMemberRepository.findFirstByHouseholdAndIdNotOrderByCreatedAtAsc(household, member.getId())).thenReturn(Optional.empty());

        householdMemberService.deleteMember(member.getId(), SUB);

        verify(householdMemberRepository, never()).save(any());
        verify(householdMemberRepository).delete(member);
    }

    @Test
    void deleteMember_throwsAccessDenied_whenNonAdminRemovesSomeoneElse() {
        Household household = new Household();
        HouseholdMember other = memberWithRole(household, HouseholdMemberRole.MEMBER);
        User caller = userWithSub(SUB);
        HouseholdMember callerMembership = memberWithRole(household, HouseholdMemberRole.MEMBER);
        callerMembership.setUser(caller);

        when(householdMemberRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(callerMembership));

        assertThatThrownBy(() -> householdMemberService.deleteMember(other.getId(), SUB))
                .isInstanceOf(AccessDeniedException.class);
        verify(householdMemberRepository, never()).delete(any());
    }

    @Test
    void deleteMember_allowsAdminRemovingSomeoneElse() {
        Household household = new Household();
        HouseholdMember other = memberWithRole(household, HouseholdMemberRole.MEMBER);
        User caller = userWithSub(SUB);
        HouseholdMember callerMembership = memberWithRole(household, HouseholdMemberRole.ADMIN);
        callerMembership.setUser(caller);

        when(householdMemberRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(callerMembership));

        householdMemberService.deleteMember(other.getId(), SUB);

        verify(householdMemberRepository).delete(other);
    }

    @Test
    void deleteMember_throwsAccessDenied_whenCallerNotMemberOfHousehold() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.MEMBER);
        User caller = userWithSub(SUB);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> householdMemberService.deleteMember(member.getId(), SUB))
                .isInstanceOf(AccessDeniedException.class);
        verify(householdMemberRepository, never()).delete(any());
    }

    @Test
    void deleteMember_throwsBadRequest_whenOpenTaskAssignmentsExist() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.MEMBER);
        User caller = userWithSub(SUB);
        member.setUser(caller);

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(userService.getUserBySub(SUB)).thenReturn(caller);
        when(householdMemberRepository.findByHouseholdAndUser(household, caller)).thenReturn(Optional.of(member));
        when(taskAssignmentRepository.existsByHouseholdMemberAndTaskCompletionIsNull(member)).thenReturn(true);

        assertThatThrownBy(() -> householdMemberService.deleteMember(member.getId(), SUB))
                .isInstanceOf(BadRequestException.class);
        verify(householdMemberRepository, never()).delete(any());
    }

    @Test
    void promoteMember_succeeds_whenCallerIsAdmin() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.MEMBER);
        member.setUser(userWithSub("auth0|target"));

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(householdMemberRepository.save(member)).thenReturn(member);

        HouseholdMemberResponseDto dto = householdMemberService.promoteMember(member.getId(), SUB);

        verify(householdService).requireAdmin(household, SUB);
        assertThat(dto.getRole()).isEqualTo(HouseholdMemberRole.ADMIN);
    }

    @Test
    void promoteMember_throwsAccessDenied_whenCallerIsNotAdmin() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.MEMBER);
        member.setUser(userWithSub("auth0|target"));

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        doThrow(new AccessDeniedException("Only a household admin can perform this action"))
                .when(householdService).requireAdmin(household, SUB);

        assertThatThrownBy(() -> householdMemberService.promoteMember(member.getId(), SUB))
                .isInstanceOf(AccessDeniedException.class);
        verify(householdMemberRepository, never()).save(any());
    }

    @Test
    void promoteMember_throwsBadRequest_whenMemberAlreadyAdmin() {
        Household household = new Household();
        HouseholdMember member = memberWithRole(household, HouseholdMemberRole.ADMIN);
        member.setUser(userWithSub("auth0|target"));

        when(householdMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> householdMemberService.promoteMember(member.getId(), SUB))
                .isInstanceOf(BadRequestException.class);
        verify(householdMemberRepository, never()).save(any());
    }

    private HouseholdMember memberWithRole(Household household, HouseholdMemberRole role) {
        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setRole(role);
        setId(member, UUID.randomUUID());
        return member;
    }

    private User userWithSub(String sub) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName("First");
        user.setLastName("Last");
        return user;
    }

    private void setId(BaseEntity entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}

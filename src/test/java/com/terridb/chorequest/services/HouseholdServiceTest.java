package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.entities.enums.HouseholdType;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdMapper;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.mappers.UserMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.HouseholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    private static final String MEMBER_SUB = "auth0|member";
    private static final String OUTSIDER_SUB = "auth0|outsider";

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private UserService userService;

    private HouseholdService householdService;

    @BeforeEach
    void setUp() {
        HouseholdMapper householdMapper = new HouseholdMapper(new HouseholdMemberMapper(new UserMapper()));
        householdService = new HouseholdService(householdRepository, householdMemberRepository, householdMapper, userService);
    }

    @Test
    void requireMembership_passes_whenUserIsMember() {
        Household household = new Household();
        User member = userWithSub(MEMBER_SUB);
        when(userService.getUserBySub(MEMBER_SUB)).thenReturn(member);
        when(householdMemberRepository.existsByHouseholdAndUserAndLeftAtIsNull(household, member)).thenReturn(true);

        householdService.requireMembership(household, MEMBER_SUB);
    }

    @Test
    void requireMembership_throwsAccessDenied_whenUserIsNotMember() {
        Household household = new Household();
        User outsider = userWithSub(OUTSIDER_SUB);
        when(userService.getUserBySub(OUTSIDER_SUB)).thenReturn(outsider);
        when(householdMemberRepository.existsByHouseholdAndUserAndLeftAtIsNull(household, outsider)).thenReturn(false);

        assertThatThrownBy(() -> householdService.requireMembership(household, OUTSIDER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireAdmin_passes_whenUserIsAdmin() {
        Household household = new Household();
        User admin = userWithSub(MEMBER_SUB);
        HouseholdMember membership = new HouseholdMember();
        membership.setRole(HouseholdMemberRole.ADMIN);
        when(userService.getUserBySub(MEMBER_SUB)).thenReturn(admin);
        when(householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, admin)).thenReturn(Optional.of(membership));

        householdService.requireAdmin(household, MEMBER_SUB);
    }

    @Test
    void requireAdmin_throwsAccessDenied_whenUserIsMemberButNotAdmin() {
        Household household = new Household();
        User member = userWithSub(MEMBER_SUB);
        HouseholdMember membership = new HouseholdMember();
        membership.setRole(HouseholdMemberRole.MEMBER);
        when(userService.getUserBySub(MEMBER_SUB)).thenReturn(member);
        when(householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, member)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> householdService.requireAdmin(household, MEMBER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireAdmin_throwsAccessDenied_whenUserIsNotMember() {
        Household household = new Household();
        User outsider = userWithSub(OUTSIDER_SUB);
        when(userService.getUserBySub(OUTSIDER_SUB)).thenReturn(outsider);
        when(householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> householdService.requireAdmin(household, OUTSIDER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateHousehold_throwsAccessDenied_whenCallerIsMemberButNotAdmin() {
        UUID id = UUID.randomUUID();
        Household household = new Household();
        User member = userWithSub(MEMBER_SUB);
        HouseholdMember membership = new HouseholdMember();
        membership.setRole(HouseholdMemberRole.MEMBER);
        when(householdRepository.findById(id)).thenReturn(Optional.of(household));
        when(userService.getUserBySub(MEMBER_SUB)).thenReturn(member);
        when(householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, member)).thenReturn(Optional.of(membership));

        HouseholdPatchDto patch = new HouseholdPatchDto();
        patch.setName("Renamed");

        assertThatThrownBy(() -> householdService.updateHousehold(id, patch, MEMBER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getHouseholdById_throwsAccessDenied_whenCallerIsNotAMember() {
        UUID id = UUID.randomUUID();
        Household household = new Household();
        User outsider = userWithSub(OUTSIDER_SUB);
        when(householdRepository.findById(id)).thenReturn(Optional.of(household));
        when(userService.getUserBySub(OUTSIDER_SUB)).thenReturn(outsider);
        when(householdMemberRepository.existsByHouseholdAndUserAndLeftAtIsNull(household, outsider)).thenReturn(false);

        assertThatThrownBy(() -> householdService.getHouseholdById(id, OUTSIDER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getHouseholdById_throwsRecordNotFound_whenHouseholdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(householdRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> householdService.getHouseholdById(id, MEMBER_SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void getHouseholdsByUser_throwsAccessDenied_whenCallerIsNotTheRequestedUser() {
        UUID userId = UUID.randomUUID();
        User requestedUser = userWithSub(MEMBER_SUB);
        when(userService.getUser(userId)).thenReturn(requestedUser);

        assertThatThrownBy(() -> householdService.getHouseholdsByUser(userId, OUTSIDER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getHouseholdsByUser_returnsOwnHouseholds_whenCallerIsTheRequestedUser() {
        UUID userId = UUID.randomUUID();
        Household household = new Household();
        household.setName("Home");
        household.setType(HouseholdType.FAMILY);
        HouseholdMember membership = new HouseholdMember();
        membership.setHousehold(household);

        User requestedUser = userWithSub(MEMBER_SUB);
        requestedUser.setMemberships(Set.of(membership));
        when(userService.getUser(userId)).thenReturn(requestedUser);

        List<HouseholdResponseDto> households = householdService.getHouseholdsByUser(userId, MEMBER_SUB);

        assertThat(households).hasSize(1);
        assertThat(households.get(0).getName()).isEqualTo("Home");
    }

    @Test
    void postHousehold_addsCreatorAsMember() {
        User creator = userWithSub(MEMBER_SUB);
        when(userService.getUserBySub(MEMBER_SUB)).thenReturn(creator);
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(householdMemberRepository.save(any(HouseholdMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdRequestDto request = new HouseholdRequestDto();
        request.setName("New Household");
        request.setType(HouseholdType.SHARED);

        HouseholdResponseDto dto = householdService.postHousehold(request, MEMBER_SUB);

        assertThat(dto.getMembers()).hasSize(1);
        assertThat(dto.getMembers().get(0).getUser().getFirstName()).isEqualTo(creator.getFirstName());

        ArgumentCaptor<HouseholdMember> memberCaptor = ArgumentCaptor.forClass(HouseholdMember.class);
        verify(householdMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getUser()).isEqualTo(creator);
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(HouseholdMemberRole.ADMIN);
    }

    @Test
    void deleteHousehold_throwsAccessDenied_whenCallerIsNotAMember() {
        UUID id = UUID.randomUUID();
        Household household = new Household();
        User outsider = userWithSub(OUTSIDER_SUB);
        when(householdRepository.findById(id)).thenReturn(Optional.of(household));
        when(userService.getUserBySub(OUTSIDER_SUB)).thenReturn(outsider);
        when(householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> householdService.deleteHousehold(id, OUTSIDER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    private User userWithSub(String sub) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName("First-" + sub);
        user.setLastName("Last");
        return user;
    }
}

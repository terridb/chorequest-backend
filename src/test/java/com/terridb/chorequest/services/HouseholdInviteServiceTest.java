package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.householdInvites.HouseholdInviteResponseDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdInvite;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdInviteStatus;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdInviteMapper;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.mappers.UserMapper;
import com.terridb.chorequest.repositories.HouseholdInviteRepository;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdInviteServiceTest {

    private static final String SUB = "auth0|admin";
    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    @Mock
    private HouseholdInviteRepository householdInviteRepository;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private HouseholdService householdService;

    @Mock
    private UserService userService;

    private HouseholdInviteService householdInviteService;

    @BeforeEach
    void setUp() {
        HouseholdInviteMapper householdInviteMapper = new HouseholdInviteMapper();
        HouseholdMemberMapper householdMemberMapper = new HouseholdMemberMapper(new UserMapper());
        householdInviteService = new HouseholdInviteService(householdInviteRepository, householdMemberRepository, householdInviteMapper, householdMemberMapper, householdService, userService);
    }

    @Test
    void createInvite_returnsExistingValidInvite_whenOneExists() {
        Household household = new Household();
        when(householdService.getHousehold(HOUSEHOLD_ID)).thenReturn(household);
        HouseholdInvite existing = invite(household, HouseholdInviteStatus.PENDING, LocalDateTime.now().plusDays(1));
        when(householdInviteRepository.findByHouseholdAndStatusAndExpiresAtAfter(eq(household), eq(HouseholdInviteStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existing));

        HouseholdInviteResponseDto dto = householdInviteService.createInvite(HOUSEHOLD_ID, SUB);

        assertThat(dto.getCode()).isEqualTo(existing.getCode());
        verify(householdService).requireAdmin(household, SUB);
        verify(householdInviteRepository, never()).save(any());
    }

    @Test
    void createInvite_generatesNewInvite_whenNoneValid() {
        Household household = new Household();
        when(householdService.getHousehold(HOUSEHOLD_ID)).thenReturn(household);
        when(householdInviteRepository.findByHouseholdAndStatusAndExpiresAtAfter(eq(household), eq(HouseholdInviteStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(householdInviteRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(userService.getUserBySub(SUB)).thenReturn(userWithSub(SUB));
        when(householdInviteRepository.save(any(HouseholdInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdInviteResponseDto dto = householdInviteService.createInvite(HOUSEHOLD_ID, SUB);

        assertThat(dto.getStatus()).isEqualTo(HouseholdInviteStatus.PENDING);
        assertThat(dto.getCode()).hasSize(8);
        assertThat(dto.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void createInvite_throwsAccessDenied_whenCallerIsNotAdmin() {
        Household household = new Household();
        when(householdService.getHousehold(HOUSEHOLD_ID)).thenReturn(household);
        doThrow(new AccessDeniedException("Only a household admin can perform this action"))
                .when(householdService).requireAdmin(household, SUB);

        assertThatThrownBy(() -> householdInviteService.createInvite(HOUSEHOLD_ID, SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void revokeInvite_setsStatusRevoked_whenCallerIsAdmin() {
        Household household = new Household();
        ReflectionTestUtils.setField(household, "id", HOUSEHOLD_ID);
        when(householdService.getHousehold(HOUSEHOLD_ID)).thenReturn(household);
        HouseholdInvite existing = invite(household, HouseholdInviteStatus.PENDING, LocalDateTime.now().plusDays(1));
        when(householdInviteRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        householdInviteService.revokeInvite(HOUSEHOLD_ID, existing.getId(), SUB);

        ArgumentCaptor<HouseholdInvite> captor = ArgumentCaptor.forClass(HouseholdInvite.class);
        verify(householdInviteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(HouseholdInviteStatus.REVOKED);
    }

    @Test
    void revokeInvite_throwsAccessDenied_whenCallerIsNotAdmin() {
        Household household = new Household();
        when(householdService.getHousehold(HOUSEHOLD_ID)).thenReturn(household);
        doThrow(new AccessDeniedException("Only a household admin can perform this action"))
                .when(householdService).requireAdmin(household, SUB);

        assertThatThrownBy(() -> householdInviteService.revokeInvite(HOUSEHOLD_ID, UUID.randomUUID(), SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void acceptInvite_succeeds_andCreatesMemberRow() {
        Household household = new Household();
        HouseholdInvite validInvite = invite(household, HouseholdInviteStatus.PENDING, LocalDateTime.now().plusDays(1));
        User user = userWithSub("auth0|joiner");
        when(householdInviteRepository.findByCode(validInvite.getCode())).thenReturn(Optional.of(validInvite));
        when(userService.getUserBySub("auth0|joiner")).thenReturn(user);
        when(householdMemberRepository.existsByHouseholdAndUser(household, user)).thenReturn(false);
        when(householdMemberRepository.save(any(HouseholdMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HouseholdMemberResponseDto dto = householdInviteService.acceptInvite(validInvite.getCode(), "auth0|joiner");

        assertThat(dto.getRole()).isEqualTo(HouseholdMemberRole.MEMBER);
    }

    @Test
    void acceptInvite_throwsRecordNotFound_whenCodeUnknown() {
        when(householdInviteRepository.findByCode("UNKNOWN1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> householdInviteService.acceptInvite("UNKNOWN1", SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void acceptInvite_throwsRecordNotFound_whenInviteExpired() {
        Household household = new Household();
        HouseholdInvite expired = invite(household, HouseholdInviteStatus.PENDING, LocalDateTime.now().minusMinutes(1));
        when(householdInviteRepository.findByCode(expired.getCode())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> householdInviteService.acceptInvite(expired.getCode(), SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void acceptInvite_throwsRecordNotFound_whenInviteRevoked() {
        Household household = new Household();
        HouseholdInvite revoked = invite(household, HouseholdInviteStatus.REVOKED, LocalDateTime.now().plusDays(1));
        when(householdInviteRepository.findByCode(revoked.getCode())).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> householdInviteService.acceptInvite(revoked.getCode(), SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void acceptInvite_throwsBadRequest_whenAlreadyMember() {
        Household household = new Household();
        HouseholdInvite validInvite = invite(household, HouseholdInviteStatus.PENDING, LocalDateTime.now().plusDays(1));
        User user = userWithSub(SUB);
        when(householdInviteRepository.findByCode(validInvite.getCode())).thenReturn(Optional.of(validInvite));
        when(userService.getUserBySub(SUB)).thenReturn(user);
        when(householdMemberRepository.existsByHouseholdAndUser(household, user)).thenReturn(true);

        assertThatThrownBy(() -> householdInviteService.acceptInvite(validInvite.getCode(), SUB))
                .isInstanceOf(BadRequestException.class);
        verify(householdMemberRepository, never()).save(any());
    }

    private HouseholdInvite invite(Household household, HouseholdInviteStatus status, LocalDateTime expiresAt) {
        HouseholdInvite invite = new HouseholdInvite();
        invite.setHousehold(household);
        invite.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invite.setStatus(status);
        invite.setExpiresAt(expiresAt);
        invite.setCreatedBy(userWithSub("auth0|creator"));
        ReflectionTestUtils.setField(invite, "id", UUID.randomUUID());
        return invite;
    }

    private User userWithSub(String sub) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName("First");
        user.setLastName("Last");
        return user;
    }
}

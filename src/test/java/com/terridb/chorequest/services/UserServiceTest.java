package com.terridb.chorequest.services;

import com.terridb.chorequest.clients.Auth0ManagementClient;
import com.terridb.chorequest.dtos.users.MeRequestDto;
import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.UserMapper;
import com.terridb.chorequest.repositories.HouseholdInviteRepository;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String OWNER_SUB = "auth0|owner";
    private static final String OTHER_SUB = "auth0|other";

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private HouseholdInviteRepository householdInviteRepository;

    @Mock
    private Auth0ManagementClient auth0ManagementClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new UserMapper(), householdMemberRepository, householdInviteRepository, auth0ManagementClient);
    }

    @Test
    void getUserById_returnsUser_whenCallerIsOwner() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseDto dto = userService.getUserById(id, OWNER_SUB);

        assertThat(dto.getFirstName()).isEqualTo(user.getFirstName());
    }

    @Test
    void getUserById_throwsAccessDenied_whenCallerIsNotOwner() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getUserById(id, OTHER_SUB))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserById_throwsRecordNotFound_whenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id, OWNER_SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void updateUser_throwsAccessDenied_whenCallerIsNotOwner() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserPatchDto patch = new UserPatchDto();
        patch.setFirstName("Hacked");

        assertThatThrownBy(() -> userService.updateUser(id, patch, OTHER_SUB))
                .isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_appliesPatch_whenCallerIsOwner() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPatchDto patch = new UserPatchDto();
        patch.setFirstName("Updated");

        UserResponseDto dto = userService.updateUser(id, patch, OWNER_SUB);

        assertThat(dto.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void deleteUser_throwsAccessDenied_whenCallerIsNotOwner() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(id, OTHER_SUB))
                .isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsBadRequest_whenUserStillHasActiveMemberships() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        user.setMemberships(Set.of(new HouseholdMember()));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(id, OWNER_SUB))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).delete(any());
        verify(auth0ManagementClient, never()).deleteUser(any());
    }

    @Test
    void deleteUser_deletesUser_whenOwnerAndNoMemberships() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, OWNER_SUB);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_deletesInvitesCreatedByUser() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, OWNER_SUB);

        verify(householdInviteRepository).deleteAllByCreatedBy(user);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_deletesAuth0User_afterDeletingUser() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, OWNER_SUB);

        InOrder inOrder = inOrder(userRepository, auth0ManagementClient);
        inOrder.verify(userRepository).flush();
        inOrder.verify(auth0ManagementClient).deleteUser(OWNER_SUB);
    }

    @Test
    void deleteUser_keepsPastMembershipsWithoutUser_whenUserLeftTheirHouseholds() {
        UUID id = UUID.randomUUID();
        User user = userWithSub(OWNER_SUB);
        HouseholdMember pastMembership = new HouseholdMember();
        pastMembership.setUser(user);
        pastMembership.setLeftAt(LocalDateTime.now());
        user.setMemberships(Set.of(pastMembership));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, OWNER_SUB);

        assertThat(pastMembership.getUser()).isNull();
        verify(householdMemberRepository).saveAll(List.of(pastMembership));
        verify(userRepository).delete(user);
    }

    @Test
    void getOrCreateCurrentUser_returnsExistingUser_withoutApplyingIncomingProfile() {
        User existing = userWithSub(OWNER_SUB);
        existing.setFirstName("Existing");
        when(userRepository.findBySub(OWNER_SUB)).thenReturn(Optional.of(existing));

        MeRequestDto profile = new MeRequestDto();
        profile.setFirstName("Ignored");

        UserResponseDto dto = userService.getOrCreateCurrentUser(OWNER_SUB, profile);

        assertThat(dto.getFirstName()).isEqualTo("Existing");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateCurrentUser_createsUser_whenSubIsUnknown() {
        when(userRepository.findBySub(OWNER_SUB)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MeRequestDto profile = new MeRequestDto();
        profile.setFirstName("Terri");
        profile.setLastName("de Boer");

        UserResponseDto dto = userService.getOrCreateCurrentUser(OWNER_SUB, profile);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getSub()).isEqualTo(OWNER_SUB);
        assertThat(dto.getFirstName()).isEqualTo("Terri");
        assertThat(dto.getLastName()).isEqualTo("de Boer");
    }

    @Test
    void getOrCreateCurrentUser_fallsBackToDefaultNames_whenProfileFieldsAreBlank() {
        when(userRepository.findBySub(OWNER_SUB)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto dto = userService.getOrCreateCurrentUser(OWNER_SUB, new MeRequestDto());

        assertThat(dto.getFirstName()).isEqualTo("New");
        assertThat(dto.getLastName()).isEqualTo("User");
    }

    @Test
    void getUserBySub_throwsRecordNotFound_whenSubIsUnknown() {
        when(userRepository.findBySub(OTHER_SUB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserBySub(OTHER_SUB))
                .isInstanceOf(RecordNotFoundException.class);
    }

    private User userWithSub(String sub) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName("First");
        user.setLastName("Last");
        return user;
    }
}

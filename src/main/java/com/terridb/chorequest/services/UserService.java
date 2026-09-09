package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.users.MeRequestDto;
import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.UserMapper;
import com.terridb.chorequest.repositories.UserRepository;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDto getUserById(UUID id, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        return userMapper.toDto(user);
    }

    public UserResponseDto updateUser(UUID id, UserPatchDto input, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);
        User patchedUser = userMapper.patch(input, user);

        return userMapper.toDto(userRepository.save(patchedUser));
    }

    public void deleteUser(UUID id, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        if (!user.getMemberships().isEmpty()) {
            throw new BadRequestException("Cannot delete a user who is still a member of a household");
        }

        userRepository.delete(user);
    }

    private void requireSelf(User user, String sub) {
        if (!user.getSub().equals(sub)) {
            throw new AccessDeniedException("Cannot access another user's account");
        }
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("User with id " + id + " not found"));
    }

    public UserResponseDto getOrCreateCurrentUser(String sub, MeRequestDto profile) {
        User user = userRepository.findBySub(sub)
                .orElseGet(() -> userRepository.save(buildNewUser(sub, profile)));

        return userMapper.toDto(user);
    }

    public User getUserBySub(String sub) {
        return userRepository.findBySub(sub).
                orElseThrow(() -> new RecordNotFoundException("User with sub " + sub + " not found"));
    }

    private User buildNewUser(String sub, MeRequestDto profile) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName(firstNonBlank(profile.getFirstName(), "New"));
        user.setLastName(firstNonBlank(profile.getLastName(), "User"));
        user.setAvatarUrl(profile.getAvatarUrl());

        return user;
    }

    private String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

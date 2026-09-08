package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.users.UserRequestDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import com.terridb.chorequest.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public User toEntity(UserRequestDto requestDto, User user) {
        if (user == null) {
            user = new User();
        }

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setAvatarUrl(requestDto.getAvatarUrl());

        return user;
    }

    public UserResponseDto toDto(User user) {
        UserResponseDto responseDto = new UserResponseDto();

        responseDto.setId(user.getId());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        responseDto.setAvatarUrl(user.getAvatarUrl());
        responseDto.setHouseholdIds(user.getMemberships().stream()
                .map(householdMember -> householdMember.getHousehold().getId())
                .collect(Collectors.toList()));

        return responseDto;
    }

    public List<UserResponseDto> toDto(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseSimpleDto toSimpleDto(User user) {
        UserResponseSimpleDto responseDto = new UserResponseSimpleDto();

        responseDto.setId(user.getId());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setAvatarUrl(user.getAvatarUrl());

        return responseDto;
    }

    public List<UserResponseSimpleDto> toSimpleDto(List<User> users) {
        return users.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }
}

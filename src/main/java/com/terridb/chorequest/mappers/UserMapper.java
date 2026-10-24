package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public User patch(UserPatchDto patchDto, User user) {
        if (patchDto.getFirstName() != null) {
            user.setFirstName(patchDto.getFirstName());
        }
        if (patchDto.getLastName() != null) {
            user.setLastName(patchDto.getLastName());
        }

        return user;
    }

    public UserResponseDto toDto(User user) {
        UserResponseDto responseDto = new UserResponseDto();

        responseDto.setId(user.getId());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        responseDto.setAvatarUrl(resolveAvatarUrl(user));
        responseDto.setHouseholdIds(user.getMemberships().stream()
                .filter(HouseholdMember::isActive)
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
        if (user == null) {
            return null;
        }

        UserResponseSimpleDto responseDto = new UserResponseSimpleDto();

        responseDto.setId(user.getId());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setAvatarUrl(resolveAvatarUrl(user));

        return responseDto;
    }

    public List<UserResponseSimpleDto> toSimpleDto(List<User> users) {
        return users.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }

    private String resolveAvatarUrl(User user) {
        if (user.getCustomAvatarUpdatedAt() != null) {
            long version = user.getCustomAvatarUpdatedAt().toEpochSecond(ZoneOffset.UTC);
            return "/api/users/" + user.getId() + "/avatar?v=" + version;
        }
        return user.getAvatarUrl();
    }
}

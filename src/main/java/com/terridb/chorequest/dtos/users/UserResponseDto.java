package com.terridb.chorequest.dtos.users;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private List<UUID> householdIds;
}

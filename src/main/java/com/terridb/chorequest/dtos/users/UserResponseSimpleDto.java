package com.terridb.chorequest.dtos.users;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserResponseSimpleDto {
    private UUID id;
    private String firstName;
    private String avatarUrl;
}

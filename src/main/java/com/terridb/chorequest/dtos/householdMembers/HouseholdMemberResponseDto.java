package com.terridb.chorequest.dtos.householdMembers;

import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HouseholdMemberResponseDto {
    private UUID id;
    private UserResponseSimpleDto user;
    private int points;
}

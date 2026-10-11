package com.terridb.chorequest.dtos.householdMembers;

import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class HouseholdMemberResponseDto {
    private UUID id;
    private UserResponseSimpleDto user;
    private int points;
    private HouseholdMemberRole role;
    private LocalDateTime leftAt;
}

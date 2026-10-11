package com.terridb.chorequest.dtos.householdInvites;

import com.terridb.chorequest.entities.enums.HouseholdInviteStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class HouseholdInviteResponseDto {
    private UUID id;
    private String code;
    private LocalDateTime expiresAt;
    private HouseholdInviteStatus status;
}
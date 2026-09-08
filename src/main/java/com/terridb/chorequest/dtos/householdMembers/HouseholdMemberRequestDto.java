package com.terridb.chorequest.dtos.householdMembers;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HouseholdMemberRequestDto {
    @NotNull
    private UUID userId;
}

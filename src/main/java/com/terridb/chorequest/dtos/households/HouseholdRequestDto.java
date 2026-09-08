package com.terridb.chorequest.dtos.households;

import com.terridb.chorequest.entities.enums.HouseholdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HouseholdRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    @NotNull(message = "Type is required")
    private HouseholdType type;

    @NotNull(message = "Creator user id is required")
    private UUID creatorUserId;

    @Size(max = 1024, message = "Photo URL must be at most 1024 characters")
    private String photoUrl;

    @Size(max = 85, message = "City must be at most 85 characters")
    private String city;

    @Size(max = 10, message = "Zipcode must be at most 10 characters")
    private String zipcode;
}
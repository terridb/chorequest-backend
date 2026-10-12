package com.terridb.chorequest.dtos.households;

import com.terridb.chorequest.entities.enums.HouseholdType;
import com.terridb.chorequest.validation.ValidTimeZone;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseholdPatchDto {
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    private HouseholdType type;

    @Size(max = 1024, message = "Photo URL must be at most 1024 characters")
    private String photoUrl;

    @Size(max = 85, message = "City must be at most 85 characters")
    private String city;

    @Size(max = 10, message = "Zipcode must be at most 10 characters")
    private String zipcode;

    @ValidTimeZone
    private String timeZone;
}

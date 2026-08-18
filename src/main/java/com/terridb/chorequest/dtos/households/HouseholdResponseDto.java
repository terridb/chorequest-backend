package com.terridb.chorequest.dtos.households;

import com.terridb.chorequest.dtos.users.UserResponseSimpleDto;
import com.terridb.chorequest.entities.enums.HouseholdType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HouseholdResponseDto {
    private String name;
    private HouseholdType type;
    private String photoUrl;
    private String city;
    private String zipcode;
    private List<UserResponseSimpleDto> members;
}

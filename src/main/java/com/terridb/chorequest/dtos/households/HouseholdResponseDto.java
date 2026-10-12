package com.terridb.chorequest.dtos.households;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.enums.HouseholdType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HouseholdResponseDto {
    private UUID id;
    private String name;
    private HouseholdType type;
    private String photoUrl;
    private String city;
    private String zipcode;
    private String timeZone;
    private List<HouseholdMemberResponseDto> members;
}

package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.entities.Household;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HouseholdMapper {
    private final HouseholdMemberMapper householdMemberMapper;

    public HouseholdMapper(HouseholdMemberMapper householdMemberMapper) {
        this.householdMemberMapper = householdMemberMapper;
    }

    public Household toEntity(HouseholdRequestDto requestDto, Household household) {
        if (household == null) {
            household = new Household();
        }

        household.setName(requestDto.getName());
        household.setType(requestDto.getType());
        household.setPhotoUrl(requestDto.getPhotoUrl());
        household.setCity(requestDto.getCity());
        household.setZipcode(requestDto.getZipcode());

        return household;
    }

    public HouseholdResponseDto toDto(Household household) {
        HouseholdResponseDto responseDto = new HouseholdResponseDto();

        responseDto.setId(household.getId());
        responseDto.setName(household.getName());
        responseDto.setType(household.getType());
        responseDto.setPhotoUrl(household.getPhotoUrl());
        responseDto.setCity(household.getCity());
        responseDto.setZipcode(household.getZipcode());
        responseDto.setMembers(householdMemberMapper.toDto(new ArrayList<>(household.getMembers())));

        return responseDto;
    }

    public List<HouseholdResponseDto> toDto(List<Household> households) {
        return households.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

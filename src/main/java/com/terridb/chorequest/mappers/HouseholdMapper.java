package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import org.springframework.stereotype.Component;


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
        if (requestDto.getTimeZone() != null) {
            household.setTimeZone(requestDto.getTimeZone());
        }

        return household;
    }

    public Household patch(HouseholdPatchDto patchDto, Household household) {
        if (patchDto.getName() != null) {
            household.setName(patchDto.getName());
        }
        if (patchDto.getType() != null) {
            household.setType(patchDto.getType());
        }
        if (patchDto.getPhotoUrl() != null) {
            household.setPhotoUrl(patchDto.getPhotoUrl());
        }
        if (patchDto.getCity() != null) {
            household.setCity(patchDto.getCity());
        }
        if (patchDto.getZipcode() != null) {
            household.setZipcode(patchDto.getZipcode());
        }
        if (patchDto.getTimeZone() != null) {
            household.setTimeZone(patchDto.getTimeZone());
        }

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
        responseDto.setTimeZone(household.getTimeZone());
        responseDto.setMembers(householdMemberMapper.toDto(household.getMembers().stream()
                .filter(HouseholdMember::isActive)
                .collect(Collectors.toList())));

        return responseDto;
    }

    public List<HouseholdResponseDto> toDto(List<Household> households) {
        return households.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

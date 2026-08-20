package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.HouseholdMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HouseholdMemberMapper {
    private final UserMapper userMapper;

    public HouseholdMemberMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public HouseholdMemberResponseDto toDto(HouseholdMember householdMember) {
        HouseholdMemberResponseDto responseDto = new HouseholdMemberResponseDto();

        responseDto.setId(householdMember.getId());
        responseDto.setUser(userMapper.toSimpleDto(householdMember.getUser()));
        responseDto.setPoints(householdMember.getPoints());

        return responseDto;
    }

    public List<HouseholdMemberResponseDto> toDto(List<HouseholdMember> householdMembers) {
        return householdMembers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

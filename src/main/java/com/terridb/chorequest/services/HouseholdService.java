package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdMapper;
import com.terridb.chorequest.repositories.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdMapper householdMapper;
    private final UserService userService;

    public HouseholdService(HouseholdRepository householdRepository, HouseholdMapper householdMapper, UserService userService) {
        this.householdRepository = householdRepository;
        this.householdMapper = householdMapper;
        this.userService = userService;
    }

    public HouseholdResponseDto getHouseholdById(UUID id) {
        return householdMapper.toDto(getHousehold(id));
    }

    public List<HouseholdResponseDto> getHouseholdsByUser(UUID id) {
        Set<HouseholdMember> memberships = userService.getUser(id).getMemberships();
        List<Household> households = memberships.stream()
                .map(HouseholdMember::getHousehold)
                .collect(Collectors.toList());

        return householdMapper.toDto(households);
    }

    public HouseholdResponseDto postHousehold(HouseholdRequestDto input) {
        Household household = householdMapper.toEntity(input, null);
        return householdMapper.toDto(householdRepository.save(household));
    }

    public HouseholdResponseDto updateHousehold(UUID id, HouseholdPatchDto input) {
        Household household = getHousehold(id);
        Household patchedHousehold = householdMapper.patch(input, household);

        return householdMapper.toDto(householdRepository.save(patchedHousehold));
    }

    public void deleteHousehold(UUID id) {
        householdRepository.delete(getHousehold(id));
    }

    public Household getHousehold(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + id + " not found"));
    }
}

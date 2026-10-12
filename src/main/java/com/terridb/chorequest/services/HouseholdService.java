package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdMapper householdMapper;
    private final UserService userService;

    public HouseholdService(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository, HouseholdMapper householdMapper, UserService userService) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdMapper = householdMapper;
        this.userService = userService;
    }

    public HouseholdResponseDto getHouseholdById(UUID id, String sub) {
        Household household = getHousehold(id);
        requireMembership(household, sub);

        return householdMapper.toDto(household);
    }

    public List<HouseholdResponseDto> getHouseholdsByUser(UUID id, String sub) {
        User user = userService.getUser(id);
        requireSelf(user, sub);

        List<Household> households = user.getMemberships().stream()
                .filter(HouseholdMember::isActive)
                .map(HouseholdMember::getHousehold)
                .collect(Collectors.toList());

        return householdMapper.toDto(households);
    }

    @Transactional
    public HouseholdResponseDto postHousehold(HouseholdRequestDto input, String sub) {
        User creator = userService.getUserBySub(sub);
        Household household = householdMapper.toEntity(input, null);
        Household savedHousehold = householdRepository.save(household);

        HouseholdMember creatorMembership = new HouseholdMember();
        creatorMembership.setHousehold(savedHousehold);
        creatorMembership.setUser(creator);
        creatorMembership.setRole(HouseholdMemberRole.ADMIN);
        householdMemberRepository.save(creatorMembership);
        savedHousehold.getMembers().add(creatorMembership);

        return householdMapper.toDto(savedHousehold);
    }

    public HouseholdResponseDto updateHousehold(UUID id, HouseholdPatchDto input, String sub) {
        Household household = getHousehold(id);
        requireAdmin(household, sub);
        Household patchedHousehold = householdMapper.patch(input, household);

        return householdMapper.toDto(householdRepository.save(patchedHousehold));
    }

    public void deleteHousehold(UUID id, String sub) {
        Household household = getHousehold(id);
        requireAdmin(household, sub);

        householdRepository.delete(household);
    }

    public Household getHousehold(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + id + " not found"));
    }

    public void requireMembership(Household household, String sub) {
        User user = userService.getUserBySub(sub);
        if (!householdMemberRepository.existsByHouseholdAndUserAndLeftAtIsNull(household, user)) {
            throw new AccessDeniedException("Not a member of this household");
        }
    }

    public void requireAdmin(Household household, String sub) {
        User user = userService.getUserBySub(sub);
        HouseholdMember membership = householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, user)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this household"));

        if (membership.getRole() != HouseholdMemberRole.ADMIN) {
            throw new AccessDeniedException("Only a household admin can perform this action");
        }
    }

    private void requireSelf(User user, String sub) {
        if (!user.getSub().equals(sub)) {
            throw new AccessDeniedException("Cannot access another user's households");
        }
    }
}

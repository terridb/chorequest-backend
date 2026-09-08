package com.terridb.chorequest.services;

import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HouseholdMemberService {
    private final HouseholdMemberRepository householdMemberRepository;

    public HouseholdMemberService(HouseholdMemberRepository householdMemberRepository) {
        this.householdMemberRepository = householdMemberRepository;
    }

    public HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household member with id " + id + " not found"));
    }
}

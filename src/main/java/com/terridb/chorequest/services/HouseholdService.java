package com.terridb.chorequest.services;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.repositories.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;

    public HouseholdService(HouseholdRepository householdRepository) {
        this.householdRepository = householdRepository;
    }

    public Household getHousehold(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + id + " not found"));
    }
}

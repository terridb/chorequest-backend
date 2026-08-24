package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {
    List<HouseholdMember> findByHouseholdOrderByCreatedAtAsc(Household household);
}

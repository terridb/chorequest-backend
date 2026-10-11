package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {
    List<HouseholdMember> findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(Household household);

    boolean existsByHouseholdAndUserAndLeftAtIsNull(Household household, User user);

    Optional<HouseholdMember> findByHouseholdAndUserAndLeftAtIsNull(Household household, User user);

    boolean existsByHouseholdAndRoleAndIdNotAndLeftAtIsNull(Household household, HouseholdMemberRole role, UUID id);

    Optional<HouseholdMember> findFirstByHouseholdAndIdNotAndLeftAtIsNullOrderByCreatedAtAsc(Household household, UUID id);
}

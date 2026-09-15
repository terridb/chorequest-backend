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
    List<HouseholdMember> findByHouseholdOrderByCreatedAtAsc(Household household);

    boolean existsByHouseholdAndUser(Household household, User user);

    Optional<HouseholdMember> findByHouseholdAndUser(Household household, User user);

    boolean existsByHouseholdAndRoleAndIdNot(Household household, HouseholdMemberRole role, UUID id);

    Optional<HouseholdMember> findFirstByHouseholdAndIdNotOrderByCreatedAtAsc(Household household, UUID id);
}

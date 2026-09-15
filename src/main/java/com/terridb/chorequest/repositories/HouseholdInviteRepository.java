package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdInvite;
import com.terridb.chorequest.entities.enums.HouseholdInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdInviteRepository extends JpaRepository<HouseholdInvite, UUID> {
    Optional<HouseholdInvite> findByCode(String code);

    Optional<HouseholdInvite> findByHouseholdAndStatusAndExpiresAtAfter(Household household, HouseholdInviteStatus status, LocalDateTime now);
}
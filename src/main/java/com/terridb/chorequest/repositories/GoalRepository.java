package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByHouseholdOrderByCreatedAtDesc(Household household);

    Optional<Goal> findByHouseholdAndStatus(Household household, GoalStatus status);
}

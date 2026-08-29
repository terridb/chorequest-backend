package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.GoalContribution;
import com.terridb.chorequest.entities.HouseholdMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {
    Set<GoalContribution> findByGoalOrderByPointsDesc(Goal goal);

    Optional<GoalContribution> findByGoalAndHouseholdMember(Goal goal, HouseholdMember householdMember);
}

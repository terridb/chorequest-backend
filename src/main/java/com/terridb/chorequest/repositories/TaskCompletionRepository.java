package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, UUID> {
    List<TaskCompletion> findByCompletedByOrderByCompletedAtDesc(HouseholdMember completedBy);

    List<TaskCompletion> findByGoalOrderByRewardedPointsDesc(Goal goal);
}
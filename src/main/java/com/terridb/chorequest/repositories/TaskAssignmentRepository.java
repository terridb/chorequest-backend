package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import com.terridb.chorequest.entities.enums.AssignmentStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {
    Set<TaskAssignment> findByTaskOrderByDueDateDesc(Task task);

    Set<TaskAssignment> findByHouseholdMemberOrderByDueDateDesc(HouseholdMember householdMember);

    @EntityGraph(attributePaths = {"task", "householdMember.user", "taskCompletion"})
    Set<TaskAssignment> findByTask_HouseholdAndStatusOrderByDueDateAsc(Household household, AssignmentStatus status);

    List<TaskAssignment> findByHouseholdMemberAndTaskCompletionIsNull(HouseholdMember householdMember);

    List<TaskAssignment> findByTaskAndStatus(Task task, AssignmentStatus status);

    Optional<TaskAssignment> findFirstByTaskOrderByCreatedAtDesc(Task task);

    @EntityGraph(attributePaths = {"taskCompletion.completedBy.user", "skippedBy.user"})
    @Query("""
            SELECT a FROM TaskAssignment a LEFT JOIN a.taskCompletion c
            WHERE a.task = :task AND a.status <> com.terridb.chorequest.entities.enums.AssignmentStatus.OPEN
            ORDER BY COALESCE(c.completedAt, a.skippedAt) DESC
            """)
    List<TaskAssignment> findHistoryByTask(Task task, Limit limit);
}
package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {
    Set<TaskAssignment> findByTaskOrderByDueDateDesc(Task task);

    Set<TaskAssignment> findByHouseholdMemberOrderByDueDateDesc(HouseholdMember householdMember);

    Set<TaskAssignment> findByTask_HouseholdAndTaskCompletionIsNullOrderByDueDateAsc(Household household);

    boolean existsByTaskAndTaskCompletionIsNull(Task task);

    List<TaskAssignment> findByHouseholdMemberAndTaskCompletionIsNull(HouseholdMember householdMember);

    Optional<TaskAssignment> findFirstByTaskOrderByCreatedAtDesc(Task task);
}
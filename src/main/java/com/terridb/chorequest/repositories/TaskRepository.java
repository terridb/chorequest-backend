package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.Frequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByHouseholdOrderByCreatedAtDesc(Household household);

    List<Task> findByFrequencyNotIn(List<Frequency> frequencies);

    boolean existsByResponsibleMemberAndStrategy(HouseholdMember responsibleMember, AssignmentStrategy strategy);

    List<Task> findByResponsibleMemberAndStrategy(HouseholdMember responsibleMember, AssignmentStrategy strategy);
}

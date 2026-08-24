package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.enums.Frequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByHouseholdOrderByCreatedAtDesc(Household household);

    List<Task> findByFrequencyNot(Frequency frequency);
}

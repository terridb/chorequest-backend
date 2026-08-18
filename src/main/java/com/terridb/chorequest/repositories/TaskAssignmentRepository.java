package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {
}
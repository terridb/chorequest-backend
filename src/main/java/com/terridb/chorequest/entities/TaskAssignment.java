package com.terridb.chorequest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_assignments")
public class TaskAssignment extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "household_member_id", nullable = false)
    private HouseholdMember householdMember;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    private LocalDate dueDate;

    @OneToOne(mappedBy = "taskAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private TaskCompletion taskCompletion;
}

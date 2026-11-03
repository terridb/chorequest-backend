package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.OPEN;

    private LocalDateTime skippedAt;

    @ManyToOne
    @JoinColumn(name = "skipped_by_id")
    private HouseholdMember skippedBy;

    @OneToOne(mappedBy = "taskAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private TaskCompletion taskCompletion;
}

package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.AssignmentStatus;
import com.terridb.chorequest.exceptions.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.OPEN;

    @Setter(AccessLevel.NONE)
    private LocalDateTime skippedAt;

    @Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "skipped_by_id")
    private HouseholdMember skippedBy;

    @OneToOne(mappedBy = "taskAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private TaskCompletion taskCompletion;

    @Setter(AccessLevel.NONE)
    @Version
    private Long version;

    public void complete() {
        requireOpen();
        status = AssignmentStatus.COMPLETED;
    }

    public void skip(HouseholdMember skippedBy, LocalDateTime skippedAt) {
        requireOpen();
        status = AssignmentStatus.SKIPPED;
        this.skippedBy = skippedBy;
        this.skippedAt = skippedAt;
    }

    public void requireOpen() {
        switch (status) {
            case COMPLETED -> throw new BadRequestException("Assignment has already been completed");
            case SKIPPED -> throw new BadRequestException("Assignment has already been skipped");
            case OPEN -> {
            }
        }
    }
}

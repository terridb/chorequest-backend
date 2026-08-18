package com.terridb.chorequest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_completions")
public class TaskCompletion extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onComplete() {
        completedAt = LocalDateTime.now();
    }

    private int rewardedPoints;

    @OneToOne
    @JoinColumn(name = "task_assignment_id", nullable = false, unique = true)
    private TaskAssignment taskAssignment;

    @ManyToOne
    @JoinColumn(name = "completed_by_id", nullable = false)
    private User completedBy;
}

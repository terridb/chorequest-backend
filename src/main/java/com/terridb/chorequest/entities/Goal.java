package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "goals")
public class Goal extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false)
    private int goalPoints;

    @Column(nullable = false)
    private String title;

    private String description;
    private LocalDateTime completionDate;
    private int currentPoints = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalStatus status = GoalStatus.ACTIVE;

    @OneToMany(mappedBy = "goal")
    private Set<GoalContribution> contributions = new HashSet<>();
}

package com.terridb.chorequest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "goal_contributions", uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "household_member_id"}))
public class GoalContribution extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne
    @JoinColumn(name = "household_member_id", nullable = false)
    private HouseholdMember householdMember;

    private int points = 0;
}

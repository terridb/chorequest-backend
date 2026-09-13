package com.terridb.chorequest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "household_members", uniqueConstraints = @UniqueConstraint(columnNames = {"household_id", "user_id"}))
public class HouseholdMember extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int points = 0;

    @OneToMany(mappedBy = "householdMember")
    private Set<TaskAssignment> assignments;
}

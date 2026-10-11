package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "household_members")
public class HouseholdMember extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int points = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HouseholdMemberRole role = HouseholdMemberRole.MEMBER;

    private LocalDateTime leftAt;

    @OneToMany(mappedBy = "householdMember")
    private Set<TaskAssignment> assignments;

    public boolean isActive() {
        return leftAt == null;
    }
}

package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.Effort;
import com.terridb.chorequest.entities.enums.Frequency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tasks")
public class Task extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Effort effort = Effort.SMALL;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Frequency frequency = Frequency.AS_NEEDED;

    @Column(nullable = false)
    private LocalDate startingDate = LocalDate.now();

    @Column(nullable = false)
    private boolean alwaysSameDay = false;

    private int defaultPoints;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskAssignment> assignments;
}

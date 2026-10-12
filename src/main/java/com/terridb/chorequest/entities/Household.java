package com.terridb.chorequest.entities;

import com.terridb.chorequest.entities.enums.HouseholdType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "households")
public class Household extends BaseEntity {
    public static final String DEFAULT_TIME_ZONE = "Europe/Amsterdam";

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HouseholdType type = HouseholdType.FAMILY;

    @Column(length = 1024)
    private String photoUrl;

    @Column(length = 85)
    private String city;

    @Column(length = 10)
    private String zipcode;

    @Column(nullable = false, length = 64)
    private String timeZone = DEFAULT_TIME_ZONE;

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HouseholdMember> members = new HashSet<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Goal> goals = new HashSet<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HouseholdInvite> invites = new HashSet<>();

    public ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }
}

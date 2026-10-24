package com.terridb.chorequest.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "users")
public class User extends BaseEntity {
    @Column(name = "auth0_sub", nullable = false, unique = true, length = 255)
    private String sub;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 1024)
    private String avatarUrl;

    private LocalDateTime customAvatarUpdatedAt;

    @OneToMany(mappedBy = "user")
    private Set<HouseholdMember> memberships = new HashSet<>();
}

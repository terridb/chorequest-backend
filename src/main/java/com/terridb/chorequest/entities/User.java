package com.terridb.chorequest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 1024)
    private String avatarUrl;

    @ManyToMany(mappedBy = "members")
    private Set<Household> households = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TaskAssignment> assignments;
}

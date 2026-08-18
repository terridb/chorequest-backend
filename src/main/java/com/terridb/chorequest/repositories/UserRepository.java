package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
package com.terridb.chorequest.repositories;

import com.terridb.chorequest.entities.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, UUID> {
}

package com.terridb.chorequest.services;

import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("User with id " + id + " not found"));
    }
}

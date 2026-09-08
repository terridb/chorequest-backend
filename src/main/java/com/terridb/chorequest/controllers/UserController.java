package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.users.MeRequestDto;
import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserById(id, jwt.getSubject()));
    }

    @PostMapping("/users/me")
    public ResponseEntity<UserResponseDto> getOrCreateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) MeRequestDto input) {
        MeRequestDto profile = input != null ? input : new MeRequestDto();

        return ResponseEntity.ok(userService.getOrCreateCurrentUser(jwt.getSubject(), profile));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserPatchDto input, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.updateUser(id, input, jwt.getSubject()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.users.MeRequestDto;
import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserRequestDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.services.UserService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users/me")
    public ResponseEntity<UserResponseDto> getOrCreateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) MeRequestDto input) {
        MeRequestDto profile = input != null ? input : new MeRequestDto();

        return ResponseEntity.ok(userService.getOrCreateCurrentUser(jwt.getSubject(), profile));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> postUser(@Valid @RequestBody UserRequestDto input) {
        UserResponseDto dto = userService.postUser(input);
        URI location = UrlUtil.buildCreatedUri("/api/users/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserPatchDto input) {
        return ResponseEntity.ok(userService.updateUser(id, input));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

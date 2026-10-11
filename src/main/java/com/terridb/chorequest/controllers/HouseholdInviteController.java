package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.householdInvites.HouseholdInvitePreviewDto;
import com.terridb.chorequest.dtos.householdInvites.HouseholdInviteResponseDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.services.HouseholdInviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HouseholdInviteController {
    private final HouseholdInviteService householdInviteService;

    public HouseholdInviteController(HouseholdInviteService householdInviteService) {
        this.householdInviteService = householdInviteService;
    }

    @PostMapping("/households/{householdId}/invites")
    public ResponseEntity<HouseholdInviteResponseDto> createInvite(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdInviteService.createInvite(householdId, jwt.getSubject()));
    }

    @GetMapping("/households/{householdId}/invites/current")
    public ResponseEntity<HouseholdInviteResponseDto> getCurrentInvite(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return householdInviteService.getCurrentInvite(householdId, jwt.getSubject())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/households/{householdId}/invites/{id}")
    public ResponseEntity<Void> revokeInvite(@PathVariable UUID householdId, @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        householdInviteService.revokeInvite(householdId, id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invites/{code}")
    public ResponseEntity<HouseholdInvitePreviewDto> previewInvite(@PathVariable String code) {
        return ResponseEntity.ok(householdInviteService.previewInvite(code));
    }

    @PostMapping("/invites/{code}/accept")
    public ResponseEntity<HouseholdMemberResponseDto> acceptInvite(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdInviteService.acceptInvite(code, jwt.getSubject()));
    }
}
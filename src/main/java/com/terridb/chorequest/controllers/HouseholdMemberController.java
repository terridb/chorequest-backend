package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberRequestDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.services.HouseholdMemberService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HouseholdMemberController {
    private final HouseholdMemberService householdMemberService;

    public HouseholdMemberController(HouseholdMemberService householdMemberService) {
        this.householdMemberService = householdMemberService;
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<HouseholdMemberResponseDto> getMemberById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdMemberService.getMemberById(id, jwt.getSubject()));
    }

    @GetMapping("/households/{householdId}/members")
    public ResponseEntity<List<HouseholdMemberResponseDto>> getMembersForHousehold(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdMemberService.getMembersForHousehold(householdId, jwt.getSubject()));
    }

    @PostMapping("/households/{householdId}/members")
    public ResponseEntity<HouseholdMemberResponseDto> postMember(@PathVariable UUID householdId, @Valid @RequestBody HouseholdMemberRequestDto input, @AuthenticationPrincipal Jwt jwt) {
        HouseholdMemberResponseDto dto = householdMemberService.postMember(householdId, input, jwt.getSubject());
        URI location = UrlUtil.buildCreatedUri("/api/members/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PostMapping("/members/{id}/promote")
    public ResponseEntity<HouseholdMemberResponseDto> promoteMember(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdMemberService.promoteMember(id, jwt.getSubject()));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        householdMemberService.deleteMember(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

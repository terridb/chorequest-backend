package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberRequestDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.services.HouseholdMemberService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<HouseholdMemberResponseDto> getMemberById(@PathVariable UUID id) {
        return ResponseEntity.ok(householdMemberService.getMemberById(id));
    }

    @GetMapping("/households/{householdId}/members")
    public ResponseEntity<List<HouseholdMemberResponseDto>> getMembersForHousehold(@PathVariable UUID householdId) {
        return ResponseEntity.ok(householdMemberService.getMembersForHousehold(householdId));
    }

    @PostMapping("/households/{householdId}/members")
    public ResponseEntity<HouseholdMemberResponseDto> postMember(@PathVariable UUID householdId, @Valid @RequestBody HouseholdMemberRequestDto input) {
        HouseholdMemberResponseDto dto = householdMemberService.postMember(householdId, input);
        URI location = UrlUtil.buildCreatedUri("/api/members/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) {
        householdMemberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}

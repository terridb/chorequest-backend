package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.services.HouseholdService;
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
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping("/households/{id}")
    public ResponseEntity<HouseholdResponseDto> getHouseholdById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdService.getHouseholdById(id, jwt.getSubject()));
    }

    @GetMapping("/users/{id}/households")
    public ResponseEntity<List<HouseholdResponseDto>> getHouseholdsByUser(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdService.getHouseholdsByUser(id, jwt.getSubject()));
    }

    @PostMapping("/households")
    public ResponseEntity<HouseholdResponseDto> postHousehold(@Valid @RequestBody HouseholdRequestDto input, @AuthenticationPrincipal Jwt jwt) {
        HouseholdResponseDto dto = householdService.postHousehold(input, jwt.getSubject());
        URI location = UrlUtil.buildCreatedUri("/api/households/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/households/{id}")
    public ResponseEntity<HouseholdResponseDto> updateHousehold(@PathVariable UUID id, @Valid @RequestBody HouseholdPatchDto input, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(householdService.updateHousehold(id, input, jwt.getSubject()));
    }

    @DeleteMapping("/households/{id}")
    public ResponseEntity<Void> deleteHousehold(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        householdService.deleteHousehold(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

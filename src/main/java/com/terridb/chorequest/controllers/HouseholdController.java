package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.households.HouseholdPatchDto;
import com.terridb.chorequest.dtos.households.HouseholdRequestDto;
import com.terridb.chorequest.dtos.households.HouseholdResponseDto;
import com.terridb.chorequest.services.HouseholdService;
import com.terridb.chorequest.utils.UrlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<HouseholdResponseDto> getHouseholdById(@PathVariable UUID id) {
        return ResponseEntity.ok(householdService.getHouseholdById(id));
    }

    @GetMapping("/users/{id}/households")
    public ResponseEntity<List<HouseholdResponseDto>> getHouseholdsByUser(@PathVariable UUID id) {
        return ResponseEntity.ok(householdService.getHouseholdsByUser(id));
    }

    @PostMapping("/households")
    public ResponseEntity<HouseholdResponseDto> postHousehold(@Valid @RequestBody HouseholdRequestDto input) {
        HouseholdResponseDto dto = householdService.postHousehold(input);
        URI location = UrlUtil.buildCreatedUri("/api/households/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/households/{id}")
    public ResponseEntity<HouseholdResponseDto> updateHousehold(@PathVariable UUID id, @Valid @RequestBody HouseholdPatchDto input) {
        return ResponseEntity.ok(householdService.updateHousehold(id, input));
    }

    @DeleteMapping("/households/{id}")
    public ResponseEntity<Void> deleteHousehold(@PathVariable UUID id) {
        householdService.deleteHousehold(id);
        return ResponseEntity.noContent().build();
    }
}

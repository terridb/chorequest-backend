package com.terridb.chorequest.controllers;

import com.terridb.chorequest.dtos.goals.GoalPatchDto;
import com.terridb.chorequest.dtos.goals.GoalRequestDto;
import com.terridb.chorequest.dtos.goals.GoalResponseDto;
import com.terridb.chorequest.services.GoalService;
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
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/goals/{id}")
    public ResponseEntity<GoalResponseDto> getGoalById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(goalService.getGoalById(id, jwt.getSubject()));
    }

    @GetMapping("/households/{householdId}/goals")
    public ResponseEntity<List<GoalResponseDto>> getGoalsForHousehold(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(goalService.getGoalsForHousehold(householdId, jwt.getSubject()));
    }

    @GetMapping("/households/{householdId}/goals/current")
    public ResponseEntity<GoalResponseDto> getCurrentGoalForHousehold(@PathVariable UUID householdId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(goalService.getCurrentGoalForHousehold(householdId, jwt.getSubject()));
    }

    @PostMapping("/households/{householdId}/goals")
    public ResponseEntity<GoalResponseDto> postGoal(@PathVariable UUID householdId,
                                                     @Valid @RequestBody GoalRequestDto input,
                                                     @AuthenticationPrincipal Jwt jwt) {
        GoalResponseDto dto = goalService.postGoal(householdId, input, jwt.getSubject());
        URI location = UrlUtil.buildCreatedUri("/api/goals/{id}", dto.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PatchMapping("/goals/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(@PathVariable UUID id, @Valid @RequestBody GoalPatchDto input, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(goalService.updateGoal(id, input, jwt.getSubject()));
    }
}

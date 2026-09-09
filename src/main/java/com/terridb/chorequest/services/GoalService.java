package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.goals.GoalPatchDto;
import com.terridb.chorequest.dtos.goals.GoalRequestDto;
import com.terridb.chorequest.dtos.goals.GoalResponseDto;
import com.terridb.chorequest.entities.Goal;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.enums.GoalStatus;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.GoalMapper;
import com.terridb.chorequest.repositories.GoalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final HouseholdService householdService;

    public GoalService(GoalRepository goalRepository, GoalMapper goalMapper, HouseholdService householdService) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.householdService = householdService;
    }

    public GoalResponseDto getGoalById(UUID id, String sub) {
        Goal goal = getGoal(id);
        householdService.requireMembership(goal.getHousehold(), sub);

        return goalMapper.toDto(goal);
    }

    public List<GoalResponseDto> getGoalsForHousehold(UUID id, String sub) {
        Household household = householdService.getHousehold(id);
        householdService.requireMembership(household, sub);

        List<Goal> goals = goalRepository.findByHouseholdOrderByCreatedAtDesc(household);

        return goalMapper.toDto(goals);
    }

    public GoalResponseDto getCurrentGoalForHousehold(UUID id, String sub) {
        Household household = householdService.getHousehold(id);
        householdService.requireMembership(household, sub);

        Goal goal = getActiveGoalForHousehold(household)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + id + " has no active goal"));

        return goalMapper.toDto(goal);
    }

    public GoalResponseDto updateGoal(UUID id, GoalPatchDto input, String sub) {
        Goal goal = getGoal(id);
        householdService.requireMembership(goal.getHousehold(), sub);

        GoalStatus inputStatus = input.getStatus();
        GoalStatus goalStatus = goal.getStatus();

        if (inputStatus != null) {
            if (inputStatus == GoalStatus.ACTIVE) {
                Optional<Goal> householdActiveGoal = getActiveGoalForHousehold(goal.getHousehold());
                if (householdActiveGoal.isPresent() && !householdActiveGoal.get().getId().equals(goal.getId())) {
                    throw new BadRequestException("Household already has an active goal");
                }
            } else if (inputStatus == GoalStatus.COMPLETED) {
                throw new BadRequestException("Manually setting a goal to completed is not allowed");
            } else if (inputStatus == GoalStatus.CANCELLED) {
                if (goalStatus == GoalStatus.COMPLETED) {
                    throw new BadRequestException("Completed goals can't be marked as cancelled");
                }
            }
        }

        Goal patchedGoal = goalMapper.patch(input, goal);
        return goalMapper.toDto(goalRepository.save(patchedGoal));
    }

    public GoalResponseDto postGoal(UUID householdId, GoalRequestDto input, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        if (getActiveGoalForHousehold(household).isPresent()) {
            throw new BadRequestException("Household already has an active goal");
        }

        Goal goal = goalMapper.toEntity(input, null);
        goal.setHousehold(household);

        return goalMapper.toDto(goalRepository.save(goal));
    }

    public Optional<Goal> getActiveGoalForHousehold(Household household) {
        return goalRepository.findByHouseholdAndStatus(household, GoalStatus.ACTIVE);
    }

    public void addPointsToGoal(Goal goal, int points) {
        goal.setCurrentPoints(goal.getCurrentPoints() + points);

        if (goal.getCurrentPoints() >= goal.getGoalPoints()) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletionDate(LocalDateTime.now());
        }

        goalRepository.save(goal);
    }

    public Goal getGoal(UUID id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Goal with id " + id + " not found"));
    }
}

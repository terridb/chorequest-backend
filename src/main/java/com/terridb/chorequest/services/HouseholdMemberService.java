package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberRequestDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HouseholdMemberService {
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdMemberMapper householdMemberMapper;
    private final HouseholdService householdService;
    private final UserService userService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;

    public HouseholdMemberService(HouseholdMemberRepository householdMemberRepository, HouseholdMemberMapper householdMemberMapper, HouseholdService householdService, UserService userService, TaskAssignmentRepository taskAssignmentRepository, TaskRepository taskRepository) {
        this.householdMemberRepository = householdMemberRepository;
        this.householdMemberMapper = householdMemberMapper;
        this.householdService = householdService;
        this.userService = userService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskRepository = taskRepository;
    }

    public HouseholdMemberResponseDto getMemberById(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        householdService.requireMembership(member.getHousehold(), sub);

        return householdMemberMapper.toDto(member);
    }

    public List<HouseholdMemberResponseDto> getMembersForHousehold(UUID householdId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        List<HouseholdMember> members = householdMemberRepository.findByHouseholdOrderByCreatedAtAsc(household);

        return householdMemberMapper.toDto(members);
    }

    public HouseholdMemberResponseDto postMember(UUID householdId, HouseholdMemberRequestDto input, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireMembership(household, sub);

        User user = userService.getUser(input.getUserId());

        if (householdMemberRepository.existsByHouseholdAndUser(household, user)) {
            throw new BadRequestException("User is already a member of this household");
        }

        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setUser(user);
        return householdMemberMapper.toDto(householdMemberRepository.save(member));
    }

    public void deleteMember(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        householdService.requireMembership(member.getHousehold(), sub);

        if (taskAssignmentRepository.existsByHouseholdMemberAndTaskCompletionIsNull(member)) {
            throw new BadRequestException("Cannot delete a household member with open task assignments");
        }

        if (taskRepository.existsByResponsibleMember(member)) {
            throw new BadRequestException("Cannot delete a household member who is responsible for a task");
        }

        householdMemberRepository.delete(member);
    }

    public HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household member with id " + id + " not found"));
    }
}

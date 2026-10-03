package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.Task;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.AssignmentStrategy;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.TaskAssignmentRepository;
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public HouseholdMemberResponseDto promoteMember(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        householdService.requireAdmin(member.getHousehold(), sub);

        if (member.getRole() == HouseholdMemberRole.ADMIN) {
            throw new BadRequestException("Member is already an admin");
        }

        member.setRole(HouseholdMemberRole.ADMIN);
        return householdMemberMapper.toDto(householdMemberRepository.save(member));
    }

    @Transactional
    public void deleteMember(UUID id, String sub) {
        HouseholdMember member = getMember(id);
        Household household = member.getHousehold();
        User caller = userService.getUserBySub(sub);
        HouseholdMember callerMembership = householdMemberRepository.findByHouseholdAndUser(household, caller)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this household"));

        boolean isSelf = callerMembership.getId().equals(member.getId());
        if (!isSelf && callerMembership.getRole() != HouseholdMemberRole.ADMIN) {
            throw new AccessDeniedException("Only a household admin can remove another member");
        }

        if (taskAssignmentRepository.existsByHouseholdMemberAndTaskCompletionIsNull(member)) {
            throw new BadRequestException("Cannot delete a household member with open task assignments");
        }

        if (taskRepository.existsByResponsibleMemberAndStrategy(member, AssignmentStrategy.SOLO)) {
            throw new BadRequestException("Cannot delete a household member who is responsible for a task");
        }

        if (member.getRole() == HouseholdMemberRole.ADMIN
                && !householdMemberRepository.existsByHouseholdAndRoleAndIdNot(household, HouseholdMemberRole.ADMIN, member.getId())) {
            householdMemberRepository.findFirstByHouseholdAndIdNotOrderByCreatedAtAsc(household, member.getId())
                    .ifPresent(successor -> {
                        successor.setRole(HouseholdMemberRole.ADMIN);
                        householdMemberRepository.save(successor);
                    });
        }

        // A rotating task only uses its responsible member to pick who goes first, so it can be cleared
        List<Task> rotatingTasks = taskRepository.findByResponsibleMemberAndStrategy(member, AssignmentStrategy.ROTATING);
        rotatingTasks.forEach(task -> task.setResponsibleMember(null));
        taskRepository.saveAll(rotatingTasks);

        householdMemberRepository.delete(member);
    }

    public HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household member with id " + id + " not found"));
    }
}

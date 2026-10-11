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
import com.terridb.chorequest.repositories.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HouseholdMemberService {
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdMemberMapper householdMemberMapper;
    private final HouseholdService householdService;
    private final UserService userService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskRepository taskRepository;

    public HouseholdMemberService(HouseholdMemberRepository householdMemberRepository, HouseholdMemberMapper householdMemberMapper, HouseholdService householdService, UserService userService, TaskAssignmentService taskAssignmentService, TaskRepository taskRepository) {
        this.householdMemberRepository = householdMemberRepository;
        this.householdMemberMapper = householdMemberMapper;
        this.householdService = householdService;
        this.userService = userService;
        this.taskAssignmentService = taskAssignmentService;
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

        List<HouseholdMember> members = householdMemberRepository.findByHouseholdAndLeftAtIsNullOrderByCreatedAtAsc(household);

        return householdMemberMapper.toDto(members);
    }

    public HouseholdMemberResponseDto promoteMember(UUID id, String sub) {
        HouseholdMember member = getActiveMember(id);
        householdService.requireAdmin(member.getHousehold(), sub);

        if (member.getRole() == HouseholdMemberRole.ADMIN) {
            throw new BadRequestException("Member is already an admin");
        }

        member.setRole(HouseholdMemberRole.ADMIN);
        return householdMemberMapper.toDto(householdMemberRepository.save(member));
    }

    @Transactional
    public void deleteMember(UUID id, String sub) {
        HouseholdMember member = getActiveMember(id);
        Household household = member.getHousehold();
        User caller = userService.getUserBySub(sub);
        HouseholdMember callerMembership = householdMemberRepository.findByHouseholdAndUserAndLeftAtIsNull(household, caller)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this household"));

        boolean isSelf = callerMembership.getId().equals(member.getId());
        if (!isSelf && callerMembership.getRole() != HouseholdMemberRole.ADMIN) {
            throw new AccessDeniedException("Only a household admin can remove another member");
        }

        if (member.getRole() == HouseholdMemberRole.ADMIN
                && !householdMemberRepository.existsByHouseholdAndRoleAndIdNotAndLeftAtIsNull(household, HouseholdMemberRole.ADMIN, member.getId())) {
            householdMemberRepository.findFirstByHouseholdAndIdNotAndLeftAtIsNullOrderByCreatedAtAsc(household, member.getId())
                    .ifPresent(successor -> {
                        successor.setRole(HouseholdMemberRole.ADMIN);
                        householdMemberRepository.save(successor);
                    });
        }

        List<Task> ownedTasks = taskRepository.findByResponsibleMember(member);
        ownedTasks.forEach(task -> {
            task.setStrategy(AssignmentStrategy.ROTATING);
            task.setResponsibleMember(null);
        });
        taskRepository.saveAll(ownedTasks);

        taskAssignmentService.reassignOpenAssignments(member);

        member.setLeftAt(LocalDateTime.now());
        householdMemberRepository.save(member);
    }

    public HouseholdMember getMember(UUID id) {
        return householdMemberRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Household member with id " + id + " not found"));
    }

    private HouseholdMember getActiveMember(UUID id) {
        HouseholdMember member = getMember(id);
        if (!member.isActive()) {
            throw new RecordNotFoundException("Household member with id " + id + " not found");
        }
        return member;
    }
}

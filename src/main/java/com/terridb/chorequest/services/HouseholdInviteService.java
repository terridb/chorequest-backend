package com.terridb.chorequest.services;

import com.terridb.chorequest.dtos.householdInvites.HouseholdInvitePreviewDto;
import com.terridb.chorequest.dtos.householdInvites.HouseholdInviteResponseDto;
import com.terridb.chorequest.dtos.householdMembers.HouseholdMemberResponseDto;
import com.terridb.chorequest.entities.Household;
import com.terridb.chorequest.entities.HouseholdInvite;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.enums.HouseholdInviteStatus;
import com.terridb.chorequest.entities.enums.HouseholdMemberRole;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.HouseholdInviteMapper;
import com.terridb.chorequest.mappers.HouseholdMemberMapper;
import com.terridb.chorequest.repositories.HouseholdInviteRepository;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class HouseholdInviteService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int INVITE_VALID_DAYS = 7;

    private final HouseholdInviteRepository householdInviteRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdInviteMapper householdInviteMapper;
    private final HouseholdMemberMapper householdMemberMapper;
    private final HouseholdService householdService;
    private final UserService userService;
    private final SecureRandom random = new SecureRandom();

    public HouseholdInviteService(HouseholdInviteRepository householdInviteRepository, HouseholdMemberRepository householdMemberRepository, HouseholdInviteMapper householdInviteMapper, HouseholdMemberMapper householdMemberMapper, HouseholdService householdService, UserService userService) {
        this.householdInviteRepository = householdInviteRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdInviteMapper = householdInviteMapper;
        this.householdMemberMapper = householdMemberMapper;
        this.householdService = householdService;
        this.userService = userService;
    }

    public HouseholdInviteResponseDto createInvite(UUID householdId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireAdmin(household, sub);

        HouseholdInvite invite = getActiveInviteForHousehold(household)
                .orElseGet(() -> {
                    HouseholdInvite newInvite = new HouseholdInvite();
                    newInvite.setHousehold(household);
                    newInvite.setCreatedBy(userService.getUserBySub(sub));
                    newInvite.setCode(generateUniqueCode());
                    newInvite.setExpiresAt(LocalDateTime.now().plusDays(INVITE_VALID_DAYS));
                    return householdInviteRepository.save(newInvite);
                });

        return householdInviteMapper.toDto(invite);
    }

    public HouseholdInviteResponseDto getCurrentInvite(UUID householdId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireAdmin(household, sub);

        HouseholdInvite invite = getActiveInviteForHousehold(household)
                .orElseThrow(() -> new RecordNotFoundException("Household with id " + householdId + " has no active invite"));

        return householdInviteMapper.toDto(invite);
    }

    public void revokeInvite(UUID householdId, UUID inviteId, String sub) {
        Household household = householdService.getHousehold(householdId);
        householdService.requireAdmin(household, sub);

        HouseholdInvite invite = getInvite(inviteId);
        if (!invite.getHousehold().getId().equals(household.getId())) {
            throw new RecordNotFoundException("Invite with id " + inviteId + " not found for this household");
        }

        invite.setStatus(HouseholdInviteStatus.REVOKED);
        householdInviteRepository.save(invite);
    }

    public HouseholdInvitePreviewDto previewInvite(String code) {
        HouseholdInvite invite = getValidInviteByCode(code);
        return householdInviteMapper.toPreviewDto(invite);
    }

    public HouseholdMemberResponseDto acceptInvite(String code, String sub) {
        HouseholdInvite invite = getValidInviteByCode(code);
        Household household = invite.getHousehold();
        User user = userService.getUserBySub(sub);

        if (householdMemberRepository.existsByHouseholdAndUser(household, user)) {
            throw new BadRequestException("Already a member of this household");
        }

        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setUser(user);
        member.setRole(HouseholdMemberRole.MEMBER);

        return householdMemberMapper.toDto(householdMemberRepository.save(member));
    }

    private HouseholdInvite getValidInviteByCode(String code) {
        HouseholdInvite invite = householdInviteRepository.findByCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Invite not found or expired"));

        if (invite.getStatus() != HouseholdInviteStatus.PENDING || invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RecordNotFoundException("Invite not found or expired");
        }

        return invite;
    }

    private Optional<HouseholdInvite> getActiveInviteForHousehold(Household household) {
        return householdInviteRepository.findByHouseholdAndStatusAndExpiresAtAfter(household, HouseholdInviteStatus.PENDING, LocalDateTime.now());
    }

    private HouseholdInvite getInvite(UUID id) {
        return householdInviteRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Invite with id " + id + " not found"));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (householdInviteRepository.findByCode(code).isPresent());

        return code;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }

        return builder.toString();
    }
}
package com.terridb.chorequest.mappers;

import com.terridb.chorequest.dtos.householdInvites.HouseholdInvitePreviewDto;
import com.terridb.chorequest.dtos.householdInvites.HouseholdInviteResponseDto;
import com.terridb.chorequest.entities.HouseholdInvite;
import org.springframework.stereotype.Component;

@Component
public class HouseholdInviteMapper {
    public HouseholdInviteResponseDto toDto(HouseholdInvite invite) {
        HouseholdInviteResponseDto responseDto = new HouseholdInviteResponseDto();

        responseDto.setId(invite.getId());
        responseDto.setCode(invite.getCode());
        responseDto.setExpiresAt(invite.getExpiresAt());
        responseDto.setStatus(invite.getStatus());

        return responseDto;
    }

    public HouseholdInvitePreviewDto toPreviewDto(HouseholdInvite invite) {
        HouseholdInvitePreviewDto previewDto = new HouseholdInvitePreviewDto();

        previewDto.setHouseholdName(invite.getHousehold().getName());

        return previewDto;
    }
}
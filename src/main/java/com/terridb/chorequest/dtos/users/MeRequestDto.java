package com.terridb.chorequest.dtos.users;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeRequestDto {
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @Size(max = 1024, message = "Avatar URL must be at most 1024 characters")
    private String avatarUrl;
}

package com.terridb.chorequest.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("https://chorequest-api");

    @Test
    void validate_succeeds_whenTokenContainsExpectedAudience() {
        Jwt jwt = jwtWithAudience(List.of("https://chorequest-api"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void validate_fails_whenTokenHasDifferentAudience() {
        Jwt jwt = jwtWithAudience(List.of("https://some-other-api"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void validate_fails_whenTokenHasNoAudience() {
        Jwt jwt = jwtWithAudience(List.of());

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    private Jwt jwtWithAudience(List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("aud", audience)
                .claim("sub", "auth0|test-user")
                .build();
    }
}

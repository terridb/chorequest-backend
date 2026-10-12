package com.terridb.chorequest.filters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private static final String USER_SUB = "auth0|user";
    private static final String OTHER_SUB = "auth0|other";
    private static final String INVITE_PATH = "/api/invites/ABCDEFGH";
    private static final int INVITE_LIMIT = 10;

    private final RateLimitFilter filter = new RateLimitFilter();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_passesRequests_untilLimitIsReached() throws Exception {
        authenticateAs(USER_SUB);

        for (int i = 0; i < INVITE_LIMIT; i++) {
            assertThat(send("GET", INVITE_PATH).passedThrough).isTrue();
        }
    }

    @Test
    void doFilter_returns429_whenLimitIsExceeded() throws Exception {
        authenticateAs(USER_SUB);
        exhaust();

        Result result = send("GET", INVITE_PATH);

        assertThat(result.passedThrough).isFalse();
        assertThat(result.response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(result.response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(result.response.getContentAsString()).contains("\"status\":429");
        assertThat(Long.parseLong(Objects.requireNonNull(result.response.getHeader("Retry-After")))).isPositive();
    }

    @Test
    void doFilter_keepsSeparateBuckets_perUser() throws Exception {
        authenticateAs(USER_SUB);
        exhaust();

        authenticateAs(OTHER_SUB);

        assertThat(send("GET", INVITE_PATH).passedThrough).isTrue();
    }

    @Test
    void doFilter_keepsSeparateBuckets_perTier() throws Exception {
        authenticateAs(USER_SUB);
        exhaust();

        assertThat(send("GET", "/api/households/123/tasks").passedThrough).isTrue();
        assertThat(send("POST", "/api/households/123/tasks").passedThrough).isTrue();
    }

    @Test
    void doFilter_skipsRateLimiting_whenNotAuthenticatedWithJwt() throws Exception {
        for (int i = 0; i < INVITE_LIMIT * 2; i++) {
            assertThat(send("GET", INVITE_PATH).passedThrough).isTrue();
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someone", null, List.of()));

        for (int i = 0; i < INVITE_LIMIT * 2; i++) {
            assertThat(send("GET", INVITE_PATH).passedThrough).isTrue();
        }
    }

    @Test
    void of_returnsInvite_forInvitePaths() {
        assertThat(RateLimitTier.of(new MockHttpServletRequest("GET", INVITE_PATH))).isEqualTo(RateLimitTier.INVITE);
        assertThat(RateLimitTier.of(new MockHttpServletRequest("POST", INVITE_PATH + "/accept"))).isEqualTo(RateLimitTier.INVITE);
    }

    @Test
    void of_returnsReadOrWrite_basedOnMethod() {
        String path = "/api/tasks/123";

        assertThat(RateLimitTier.of(new MockHttpServletRequest("GET", path))).isEqualTo(RateLimitTier.READ);
        assertThat(RateLimitTier.of(new MockHttpServletRequest("POST", path))).isEqualTo(RateLimitTier.WRITE);
        assertThat(RateLimitTier.of(new MockHttpServletRequest("PATCH", path))).isEqualTo(RateLimitTier.WRITE);
        assertThat(RateLimitTier.of(new MockHttpServletRequest("DELETE", path))).isEqualTo(RateLimitTier.WRITE);
    }

    private void authenticateAs(String sub) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", sub)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private void exhaust() throws Exception {
        for (int i = 0; i < RateLimitFilterTest.INVITE_LIMIT; i++) {
            send("GET", RateLimitFilterTest.INVITE_PATH);
        }
    }

    private Result send(String method, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        return new Result(response, chain.getRequest() != null);
    }

    private record Result(MockHttpServletResponse response, boolean passedThrough) {
    }
}

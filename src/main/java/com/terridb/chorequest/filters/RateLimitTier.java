package com.terridb.chorequest.filters;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

public enum RateLimitTier {
    INVITE(10),
    WRITE(60),
    READ(300);

    private final int perMinute;

    RateLimitTier(int perMinute) {
        this.perMinute = perMinute;
    }

    public Bucket newBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(perMinute)
                        .refillGreedy(perMinute, Duration.ofMinutes(1)))
                .build();
    }

    public static RateLimitTier of(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/invites/")) return INVITE;
        return "GET".equals(request.getMethod()) ? READ : WRITE;
    }
}
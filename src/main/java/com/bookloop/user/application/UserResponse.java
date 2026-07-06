package com.bookloop.user.application;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String bio,
        String city,
        String state,
        String addressLine,
        String neighborhood,
        String postalCode,
        boolean profileCompleted,
        int penaltiesCount,
        double ratingAvg,
        int ratingCount,
        String role,
        Instant createdAt,
        Instant updatedAt
) {}

package com.bookloop.organization.application;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String code,
        String name,
        String description,
        String avatarUrl,
        String plan,
        int memberLimit,
        String status,
        String myRole,
        Instant createdAt
) {}

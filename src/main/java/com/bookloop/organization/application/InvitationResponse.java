package com.bookloop.organization.application;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        String email,
        String token,
        String role,
        String status,
        Instant expiresAt,
        Instant createdAt
) {}

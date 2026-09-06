package com.bookloop.organization.application;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID membershipId,
        UUID userId,
        String name,
        String email,
        String avatarUrl,
        String role,
        Instant since
) {}

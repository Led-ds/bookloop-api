package com.bookloop.review.application;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        int rating,
        String comment,
        UUID authorId,
        String authorName,
        String authorAvatarUrl,
        String targetType,
        UUID targetId,
        String targetName,
        Instant createdAt
) {}

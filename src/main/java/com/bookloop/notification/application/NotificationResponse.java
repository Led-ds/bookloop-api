package com.bookloop.notification.application;

import com.bookloop.notification.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        UUID actorUserId,
        String targetType,
        UUID targetId,
        String actionUrl,
        boolean read,
        Instant createdAt,
        Instant readAt
) {}

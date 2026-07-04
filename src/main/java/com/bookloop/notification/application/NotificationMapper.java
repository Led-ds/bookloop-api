package com.bookloop.notification.application;

import com.bookloop.notification.domain.Notification;

/** Mapeamento entidade -> DTO. Manual e sem estado. */
public final class NotificationMapper {

    private NotificationMapper() {}

    public static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getActorUserId(),
                n.getTargetType(),
                n.getTargetId(),
                n.getActionUrl(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}

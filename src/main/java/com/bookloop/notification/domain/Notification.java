package com.bookloop.notification.domain;

import com.bookloop.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Notificação interna, persistida e consultável. Comportamento (marcar como lida,
 * checar propriedade) vive aqui — o serviço orquestra, não seta estado direto.
 */
@Getter
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient_user_id"),
        @Index(name = "idx_notifications_read", columnList = "is_read"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at"),
        @Index(name = "idx_notifications_recipient_read", columnList = "recipient_user_id, is_read")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "target_type", length = 40)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "action_url", length = 300)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private Instant readAt;

    public static Notification create(UUID recipientUserId, UUID actorUserId, NotificationType type,
                                      String title, String message,
                                      String targetType, UUID targetId, String actionUrl) {
        Notification n = new Notification();
        n.recipientUserId = recipientUserId;
        n.actorUserId = actorUserId;
        n.type = type;
        n.title = title;
        n.message = message;
        n.targetType = targetType;
        n.targetId = targetId;
        n.actionUrl = actionUrl;
        n.read = false;
        return n;
    }

    /** Idempotente: marcar duas vezes não altera o readAt original. */
    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = Instant.now();
        }
    }

    public boolean belongsTo(UUID userId) {
        return recipientUserId.equals(userId);
    }
}

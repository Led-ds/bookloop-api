package com.bookloop.notification.application;

import com.bookloop.notification.domain.Notification;
import com.bookloop.notification.domain.NotificationRepository;
import com.bookloop.notification.domain.NotificationType;
import com.bookloop.shared.application.PageResponse;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /** Cria e persiste uma notificação. Chamado pela camada de aplicação (listeners de eventos). */
    @Transactional
    public Notification create(UUID organizationId, UUID recipientUserId, UUID actorUserId, NotificationType type,
                               String title, String message,
                               String targetType, UUID targetId, String actionUrl) {
        Notification notification = Notification.create(
                organizationId, recipientUserId, actorUserId, type, title, message, targetType, targetId, actionUrl);
        Notification saved = repository.save(notification);
        log.info("Notificação criada: id={} recipientId={} type={}", saved.getId(), recipientUserId, type);
        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listForUser(UUID userId, Pageable pageable) {
        return PageResponse.from(
                repository.findByRecipientUserId(userId, pageable).map(NotificationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listUnread(UUID userId) {
        return repository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(NotificationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationCountResponse countUnread(UUID userId) {
        return new NotificationCountResponse(repository.countByRecipientUserIdAndReadFalse(userId));
    }

    @Transactional
    public MarkNotificationReadResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação", notificationId));
        if (!notification.belongsTo(userId)) {
            // Nunca permitir acessar notificação de outro usuário.
            log.warn("Acesso negado a notificação de outro usuário: notificationId={} userId={}",
                    notificationId, userId);
            throw new ForbiddenOperationException("Você não pode acessar esta notificação.");
        }
        notification.markAsRead();
        log.info("Notificação marcada como lida: id={} userId={}", notificationId, userId);
        return new MarkNotificationReadResponse(
                notification.getId(), notification.isRead(), notification.getReadAt());
    }

    @Transactional
    public NotificationCountResponse markAllAsRead(UUID userId) {
        int updated = repository.markAllAsRead(userId, Instant.now());
        log.info("Notificações marcadas como lidas: userId={} count={}", userId, updated);
        return new NotificationCountResponse(updated);
    }
}

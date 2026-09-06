package com.bookloop.notification.application;

import com.bookloop.notification.domain.Notification;
import com.bookloop.notification.domain.NotificationRepository;
import com.bookloop.notification.domain.NotificationType;
import com.bookloop.shared.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    private Notification ownedBy(UUID recipient) {
        return Notification.create(UUID.randomUUID(), recipient, null, NotificationType.SYSTEM,
                "Título", "Mensagem", null, null, null);
    }

    @Test
    void createPersistsAndReturnsNotification() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Notification saved = service.create(UUID.randomUUID(), UUID.randomUUID(), null, NotificationType.SYSTEM,
                "t", "m", null, null, null);
        assertEquals(NotificationType.SYSTEM, saved.getType());
        verify(repository).save(any());
    }

    @Test
    void userCannotMarkAnotherUsersNotification() {
        Notification n = ownedBy(UUID.randomUUID());
        when(repository.findById(any())).thenReturn(Optional.of(n));
        UUID intruder = UUID.randomUUID();
        assertThrows(ForbiddenOperationException.class,
                () -> service.markAsRead(intruder, UUID.randomUUID()));
    }

    @Test
    void ownerCanMarkOwnNotificationAsRead() {
        UUID owner = UUID.randomUUID();
        Notification n = ownedBy(owner);
        when(repository.findById(any())).thenReturn(Optional.of(n));
        service.markAsRead(owner, UUID.randomUUID());
        assertTrue(n.isRead());
    }

    @Test
    void countUnreadDelegatesToRepository() {
        UUID user = UUID.randomUUID();
        when(repository.countByRecipientUserIdAndReadFalse(user)).thenReturn(3L);
        assertEquals(3L, service.countUnread(user).count());
    }
}

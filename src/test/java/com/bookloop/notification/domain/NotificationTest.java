package com.bookloop.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationTest {

    private Notification sample(UUID recipient) {
        return Notification.create(recipient, null, NotificationType.SYSTEM,
                "Título", "Mensagem", null, null, null);
    }

    @Test
    void newNotificationStartsUnread() {
        Notification n = sample(UUID.randomUUID());
        assertFalse(n.isRead());
        assertNull(n.getReadAt());
    }

    @Test
    void markAsReadSetsFlagAndTimestamp() {
        Notification n = sample(UUID.randomUUID());
        n.markAsRead();
        assertTrue(n.isRead());
        assertNotNull(n.getReadAt());
    }

    @Test
    void markAsReadIsIdempotent() {
        Notification n = sample(UUID.randomUUID());
        n.markAsRead();
        Instant first = n.getReadAt();
        n.markAsRead();
        assertEquals(first, n.getReadAt());
    }

    @Test
    void belongsToOnlyItsRecipient() {
        UUID recipient = UUID.randomUUID();
        Notification n = sample(recipient);
        assertTrue(n.belongsTo(recipient));
        assertFalse(n.belongsTo(UUID.randomUUID()));
    }
}

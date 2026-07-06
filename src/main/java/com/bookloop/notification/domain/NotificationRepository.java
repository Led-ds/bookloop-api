package com.bookloop.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientUserId(UUID recipientUserId, Pageable pageable);

    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(UUID recipientUserId);

    long countByRecipientUserIdAndReadFalse(UUID recipientUserId);

    @Modifying
    @Query("update Notification n set n.read = true, n.readAt = :now " +
            "where n.recipientUserId = :userId and n.read = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") Instant now);
}

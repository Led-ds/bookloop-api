package com.bookloop.notification.domain;

/** Tipos de notificação interna do BookLoop. */
public enum NotificationType {
    RENTAL_REQUESTED,
    RENTAL_APPROVED,
    RENTAL_REJECTED,
    RENTAL_RETURNED,
    RESERVATION_OFFERED,
    RESERVATION_EXPIRED,
    RENTAL_REQUEST_EXPIRED,
    RENEWAL_REQUESTED,
    RENEWAL_UPDATED,
    BOOK_CREATED,
    USER_REGISTERED,
    SYSTEM
}

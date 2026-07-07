package com.bookloop.reservation.application;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookCoverUrl,
        String status,
        int position,
        Instant offerExpiresAt
) {}

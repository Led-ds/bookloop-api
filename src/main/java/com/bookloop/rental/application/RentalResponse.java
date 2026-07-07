package com.bookloop.rental.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookCoverUrl,
        UUID renterId,
        String renterName,
        UUID ownerId,
        String ownerName,
        String message,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate returnDate,
        String status,
        boolean termAccepted,
        Instant termSignedAt,
        String termSignerName,
        String renewalStatus,
        LocalDate renewalRequestedUntil,
        Instant createdAt
) {}

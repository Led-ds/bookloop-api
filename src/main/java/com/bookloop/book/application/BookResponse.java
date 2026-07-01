package com.bookloop.book.application;

import java.time.Instant;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String isbn,
        String genre,
        String description,
        String condition,
        String coverUrl,
        boolean isPublic,
        String status,
        BookOwnerView owner,
        Instant createdAt
) {}

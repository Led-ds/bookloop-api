package com.bookloop.book.application;

import java.util.UUID;

/** Compact shape for list/grid rendering. */
public record BookSummaryResponse(
        UUID id,
        String title,
        String author,
        String genre,
        String condition,
        String coverUrl,
        String status,
        String ownerName,
        double ratingAvg,
        int ratingCount
) {}

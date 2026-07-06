package com.bookloop.review.application;

import java.util.UUID;

/** Leitor com melhor reputação (para a vitrine pública). */
public record TopReaderResponse(
        UUID id,
        String name,
        String avatarUrl,
        double ratingAvg,
        int ratingCount
) {}

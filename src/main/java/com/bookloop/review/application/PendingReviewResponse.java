package com.bookloop.review.application;

import java.util.UUID;

/** O que o usuário logado ainda pode avaliar num aluguel já devolvido. */
public record PendingReviewResponse(
        UUID rentalId,
        UUID bookId,
        String bookTitle,
        String bookCoverUrl,
        UUID counterpartId,
        String counterpartName,
        boolean canReviewBook,
        boolean canReviewUser
) {}

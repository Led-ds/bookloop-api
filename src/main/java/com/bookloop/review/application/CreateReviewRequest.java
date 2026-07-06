package com.bookloop.review.application;

import com.bookloop.review.domain.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Cria uma avaliação. Para {@code type=BOOK} o livro é derivado do aluguel;
 * para {@code type=USER} é obrigatório informar {@code targetUserId} (a contraparte).
 */
public record CreateReviewRequest(
        @NotNull UUID rentalId,
        @NotNull ReviewType type,
        UUID targetUserId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 150, message = "O comentário deve ter no máximo 150 caracteres.") String comment
) {}

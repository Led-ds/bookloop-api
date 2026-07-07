package com.bookloop.review.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Edita uma avaliação existente (apenas o autor). */
public record UpdateReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 150, message = "O comentário deve ter no máximo 150 caracteres.") String comment
) {}

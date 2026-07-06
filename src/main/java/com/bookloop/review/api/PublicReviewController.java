package com.bookloop.review.api;

import com.bookloop.review.application.ReviewResponse;
import com.bookloop.review.application.ReviewService;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.shared.application.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Public", description = "Vitrine pública (sem autenticação)")
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Avaliações de um livro")
    @GetMapping("/books/{id}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> bookReviews(
            @PathVariable UUID id, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(reviewService.bookReviews(id, pageable));
    }

    @Operation(summary = "Avaliações recebidas por uma pessoa")
    @GetMapping("/users/{id}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> userReviews(
            @PathVariable UUID id, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(reviewService.userReviews(id, pageable));
    }
}

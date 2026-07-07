package com.bookloop.review.api;

import com.bookloop.review.application.CreateReviewRequest;
import com.bookloop.review.application.PendingReviewResponse;
import com.bookloop.review.application.ReviewResponse;
import com.bookloop.review.application.ReviewService;
import com.bookloop.review.application.UpdateReviewRequest;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reviews", description = "Avaliações de livros e de pessoas (após devolução)")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Criar avaliação (1..5 estrelas + comentário até 150 caracteres)")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody CreateReviewRequest req) {
        var created = reviewService.create(CurrentUser.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Avaliação registrada. Obrigado por fortalecer a confiança da comunidade."));
    }

    @Operation(summary = "Editar a própria avaliação")
    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest req) {
        return ApiResponse.ok(reviewService.update(CurrentUser.id(), id, req.rating(), req.comment()),
                "Avaliação atualizada.");
    }

    @Operation(summary = "O que ainda posso avaliar (aluguéis devolvidos)")
    @GetMapping("/pending")
    public ApiResponse<List<PendingReviewResponse>> pending() {
        return ApiResponse.ok(reviewService.pending(CurrentUser.id()));
    }
}

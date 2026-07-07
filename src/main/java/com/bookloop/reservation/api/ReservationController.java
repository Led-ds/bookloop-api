package com.bookloop.reservation.api;

import com.bookloop.reservation.application.CreateReservationRequest;
import com.bookloop.reservation.application.ReservationResponse;
import com.bookloop.reservation.application.ReservationService;
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

@Tag(name = "Reservations", description = "Fila de reserva de livros")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Entrar na fila de reserva de um livro")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(@Valid @RequestBody CreateReservationRequest req) {
        var r = reservationService.create(CurrentUser.id(), req.bookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(r, "Você entrou na fila."));
    }

    @Operation(summary = "Minhas reservas")
    @GetMapping("/mine")
    public ApiResponse<List<ReservationResponse>> mine() {
        return ApiResponse.ok(reservationService.mine(CurrentUser.id()));
    }

    @Operation(summary = "Aceitar a oferta (gera uma solicitação de aluguel ao dono)")
    @PostMapping("/{id}/accept")
    public ApiResponse<ReservationResponse> accept(@PathVariable UUID id) {
        return ApiResponse.ok(reservationService.accept(CurrentUser.id(), id), "Reserva aceita.");
    }

    @Operation(summary = "Recusar a oferta (passa a vez ao próximo da fila)")
    @PostMapping("/{id}/decline")
    public ApiResponse<ReservationResponse> decline(@PathVariable UUID id) {
        return ApiResponse.ok(reservationService.decline(CurrentUser.id(), id), "Você recusou a oferta.");
    }

    @Operation(summary = "Sair da fila")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> leave(@PathVariable UUID id) {
        reservationService.leave(CurrentUser.id(), id);
        return ApiResponse.<Void>ok(null, "Você saiu da fila.");
    }
}

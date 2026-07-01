package com.bookloop.rental.api;

import com.bookloop.rental.application.*;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.shared.application.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Rentals", description = "Fluxo de solicitação, aprovação e devolução de aluguéis")
@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @Operation(summary = "Solicitar aluguel (com termo de responsabilidade assinado)")
    @PostMapping
    public ResponseEntity<ApiResponse<RentalResponse>> request(@Valid @RequestBody CreateRentalRequest req) {
        var created = rentalService.request(CurrentUser.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Solicitação enviada ao dono do livro."));
    }

    @Operation(summary = "Aluguéis em que sou o leitor")
    @GetMapping("/mine")
    public ApiResponse<PageResponse<RentalResponse>> mine(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(rentalService.myRentals(CurrentUser.id(), pageable));
    }

    @Operation(summary = "Aluguéis dos meus livros (sou o dono)")
    @GetMapping("/lendings")
    public ApiResponse<PageResponse<RentalResponse>> lendings(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(rentalService.myLendings(CurrentUser.id(), pageable));
    }

    @Operation(summary = "Aprovar solicitação (dono)")
    @PostMapping("/{id}/approve")
    public ApiResponse<RentalResponse> approve(@PathVariable UUID id) {
        return ApiResponse.ok(rentalService.approve(CurrentUser.id(), id), "Solicitação aprovada.");
    }

    @Operation(summary = "Rejeitar solicitação (dono)")
    @PostMapping("/{id}/reject")
    public ApiResponse<RentalResponse> reject(@PathVariable UUID id) {
        return ApiResponse.ok(rentalService.reject(CurrentUser.id(), id), "Solicitação rejeitada.");
    }

    @Operation(summary = "Confirmar entrega e ativar aluguel (dono)")
    @PostMapping("/{id}/activate")
    public ApiResponse<RentalResponse> activate(@PathVariable UUID id) {
        return ApiResponse.ok(rentalService.activate(CurrentUser.id(), id), "Aluguel ativo.");
    }

    @Operation(summary = "Cancelar solicitação pendente (leitor)")
    @PostMapping("/{id}/cancel")
    public ApiResponse<RentalResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.ok(rentalService.cancel(CurrentUser.id(), id), "Solicitação cancelada.");
    }

    @Operation(summary = "Registrar devolução (dono ou leitor)")
    @PostMapping("/{id}/return")
    public ApiResponse<RentalResponse> returnBook(@PathVariable UUID id) {
        return ApiResponse.ok(rentalService.returnBook(CurrentUser.id(), id), "Devolução registrada.");
    }

    @Operation(summary = "Aprovar renovação estendendo a data de devolução (dono)")
    @PostMapping("/{id}/renew")
    public ApiResponse<RentalResponse> renew(@PathVariable UUID id, @Valid @RequestBody RenewRentalRequest req) {
        return ApiResponse.ok(rentalService.renew(CurrentUser.id(), id, req), "Aluguel renovado.");
    }
}

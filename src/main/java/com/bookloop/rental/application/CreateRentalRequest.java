package com.bookloop.rental.application;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRentalRequest(
        @NotNull UUID bookId,
        @Size(max = 500) String message,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull @Future LocalDate endDate,
        @AssertTrue(message = "É necessário aceitar o Termo de Responsabilidade")
        boolean termAccepted,
        @NotBlank @Size(max = 120) String signerName
) {}

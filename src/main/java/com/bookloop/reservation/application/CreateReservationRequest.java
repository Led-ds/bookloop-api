package com.bookloop.reservation.application;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(@NotNull UUID bookId) {}

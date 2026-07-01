package com.bookloop.rental.application;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RenewRentalRequest(@NotNull @Future LocalDate newEndDate) {}

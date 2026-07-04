package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: EmprestimoSolicitado. */
public record RentalRequestedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId) {}

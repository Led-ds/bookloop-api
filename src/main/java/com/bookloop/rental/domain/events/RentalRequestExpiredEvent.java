package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: SolicitacaoExpirada (dono não respondeu no prazo). */
public record RentalRequestExpiredEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId) {}

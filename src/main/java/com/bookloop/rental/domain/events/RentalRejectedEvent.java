package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: SolicitacaoRecusada. */
public record RentalRejectedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId) {}

package com.bookloop.rental.domain.events;

import java.time.LocalDate;
import java.util.UUID;

/** Domain event: RenovacaoSolicitada (leitor pediu; aguarda o dono). */
public record RenewalRequestedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId, LocalDate newEndDate) {}

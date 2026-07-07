package com.bookloop.rental.domain.events;

import java.time.LocalDate;
import java.util.UUID;

/** Domain event: RenovacaoResolvida (dono aprovou ou rejeitou). */
public record RenewalResolvedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId,
                                   boolean approved, LocalDate newEndDate) {}

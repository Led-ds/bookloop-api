package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: LivroDevolvido. */
public record BookReturnedEvent(UUID rentalId, UUID bookId, UUID renterId, boolean wasLate) {}

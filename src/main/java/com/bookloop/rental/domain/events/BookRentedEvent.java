package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: LivroAlugado. */
public record BookRentedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId) {}

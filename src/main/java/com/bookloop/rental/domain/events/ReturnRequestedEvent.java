package com.bookloop.rental.domain.events;

import java.util.UUID;

/** Domain event: DevolucaoSolicitada (leitor marcou "devolvi"; aguarda o dono confirmar). */
public record ReturnRequestedEvent(UUID rentalId, UUID bookId, UUID renterId, UUID ownerId) {}

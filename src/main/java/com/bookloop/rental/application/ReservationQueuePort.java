package com.bookloop.rental.application;

import java.util.UUID;

/**
 * Porta para o módulo de aluguel consultar a fila de reserva sem depender do
 * módulo de reserva (evita ciclo: reservation já depende de rental). O adapter
 * é implementado em com.bookloop.reservation.application.
 */
public interface ReservationQueuePort {
    boolean hasActiveQueue(UUID bookId);
}

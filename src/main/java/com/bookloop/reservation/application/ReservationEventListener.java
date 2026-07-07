package com.bookloop.reservation.application;

import com.bookloop.rental.domain.events.BookReturnedEvent;
import com.bookloop.rental.domain.events.RentalRejectedEvent;
import com.bookloop.rental.domain.events.RentalRequestExpiredEvent;
import com.bookloop.book.domain.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Quando um livro é liberado (devolução ou recusa de solicitação), oferece ao
 * próximo da fila em vez de deixá-lo simplesmente disponível. Roda na mesma
 * transação do evento (como o NotificationEventListener).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationService reservationService;
    private final BookRepository bookRepository;

    @EventListener
    public void onBookReturned(BookReturnedEvent e) {
        bookRepository.findById(e.bookId()).ifPresent(reservationService::offerNextOrRelease);
    }

    @EventListener
    public void onRentalRejected(RentalRejectedEvent e) {
        bookRepository.findById(e.bookId()).ifPresent(reservationService::offerNextOrRelease);
    }
    @EventListener
    public void onRentalRequestExpired(RentalRequestExpiredEvent e) {
        bookRepository.findById(e.bookId()).ifPresent(reservationService::offerNextOrRelease);
    }
}


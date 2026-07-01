package com.bookloop.rental.application;

import com.bookloop.rental.domain.events.BookRentedEvent;
import com.bookloop.rental.domain.events.BookReturnedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Side effects of domain events live here, decoupled from the transaction that
 * raised them. Today it logs (structured); tomorrow it sends notifications.
 */
@Slf4j
@Component
public class RentalEventListener {

    @EventListener
    public void onBookRented(BookRentedEvent e) {
        log.info("event=LivroAlugado rentalId={} bookId={} renterId={} ownerId={}",
                e.rentalId(), e.bookId(), e.renterId(), e.ownerId());
    }

    @EventListener
    public void onBookReturned(BookReturnedEvent e) {
        log.info("event=LivroDevolvido rentalId={} bookId={} renterId={} wasLate={}",
                e.rentalId(), e.bookId(), e.renterId(), e.wasLate());
    }
}

package com.bookloop.notification.application;

import com.bookloop.notification.domain.NotificationType;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.events.BookRentedEvent;
import com.bookloop.rental.domain.events.BookReturnedEvent;
import com.bookloop.rental.domain.events.RentalRejectedEvent;
import com.bookloop.rental.domain.events.RentalRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Traduz eventos de domínio do aluguel em notificações internas. Mantém o módulo de
 * aluguel ignorante quanto a notificações (só publica eventos). Roda na mesma transação
 * do evento (como o RentalEventListener existente); ver README para a evolução AFTER_COMMIT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final RentalRepository rentalRepository;

    @EventListener
    public void onRentalRequested(RentalRequestedEvent e) {
        rentalRepository.findById(e.rentalId()).ifPresent(r -> notificationService.create(
                r.getOwner().getId(), r.getRenter().getId(), NotificationType.RENTAL_REQUESTED,
                "Nova solicitação de empréstimo",
                r.getRenter().getName() + " solicitou o empréstimo de " + r.getBook().getTitle() + ".",
                "RENTAL", r.getId(), "/app/lendings"));
    }

    @EventListener
    public void onRentalApproved(BookRentedEvent e) {
        rentalRepository.findById(e.rentalId()).ifPresent(r -> notificationService.create(
                r.getRenter().getId(), r.getOwner().getId(), NotificationType.RENTAL_APPROVED,
                "Empréstimo aprovado",
                r.getOwner().getName() + " aprovou seu empréstimo de " + r.getBook().getTitle() + ".",
                "RENTAL", r.getId(), "/app/rentals"));
    }

    @EventListener
    public void onRentalRejected(RentalRejectedEvent e) {
        rentalRepository.findById(e.rentalId()).ifPresent(r -> notificationService.create(
                r.getRenter().getId(), r.getOwner().getId(), NotificationType.RENTAL_REJECTED,
                "Solicitação recusada",
                r.getOwner().getName() + " recusou sua solicitação de " + r.getBook().getTitle() + ".",
                "RENTAL", r.getId(), "/app/rentals"));
    }

    @EventListener
    public void onRentalReturned(BookReturnedEvent e) {
        rentalRepository.findById(e.rentalId()).ifPresent(r -> notificationService.create(
                r.getOwner().getId(), r.getRenter().getId(), NotificationType.RENTAL_RETURNED,
                "Livro devolvido",
                r.getBook().getTitle() + " foi devolvido.",
                "RENTAL", r.getId(), "/app/lendings"));
    }
}

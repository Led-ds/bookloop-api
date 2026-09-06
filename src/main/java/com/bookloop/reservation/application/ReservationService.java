package com.bookloop.reservation.application;

import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.notification.application.NotificationService;
import com.bookloop.notification.domain.NotificationType;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.events.RentalRequestedEvent;
import com.bookloop.reservation.domain.Reservation;
import com.bookloop.reservation.domain.ReservationRepository;
import com.bookloop.reservation.domain.ReservationStatus;
import com.bookloop.shared.exception.ConflictException;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Fila de reserva de livros. Unifica o item 4 (fila) com a garantia do item 6:
 * enquanto o livro está ocupado, os interessados entram em ordem; ao liberar, o
 * primeiro recebe uma oferta com prazo (aceitar gera uma solicitação de aluguel).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final List<ReservationStatus> IN_QUEUE =
            List.of(ReservationStatus.WAITING, ReservationStatus.OFFERED);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;
    private final ReservationMapper mapper;
    private final ApplicationEventPublisher events;

    @Transactional
    public ReservationResponse create(UUID userId, UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        if (book.isAvailable()) {
            throw new ConflictException(
                    "Este livro está disponível — é só solicitar o aluguel.", "BOOK_AVAILABLE");
        }
        if (reservationRepository.existsByBookIdAndUserIdAndStatusIn(bookId, userId, IN_QUEUE)) {
            throw new ConflictException("Você já está na fila deste livro.", "ALREADY_IN_QUEUE");
        }

        Reservation reservation = Reservation.waiting(book, user);
        reservationRepository.save(reservation);
        log.info("Reserva criada: id={} bookId={} userId={}", reservation.getId(), bookId, userId);
        return mapper.toResponse(reservation, positionOf(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> mine(UUID userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(r -> mapper.toResponse(r, positionOf(r)))
                .toList();
    }

    @Transactional
    public ReservationResponse accept(UUID userId, UUID reservationId) {
        Reservation reservation = loadOwned(userId, reservationId);
        reservation.accept();   // valida OFFERED + não expirada

        Book book = reservation.getBook();
        User renter = reservation.getUser();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(DEFAULT_LOAN_DAYS);

        // Opção (B): aceitar gera uma solicitação PENDING; o dono aprova/rejeita.
        // O livro já está segurado (RESERVED) desde a oferta, por isso fromReservation.
        Rental rental = Rental.fromReservation(book, renter, book.getOwner(), start, end, renter.getName());
        rentalRepository.save(rental);
        events.publishEvent(new RentalRequestedEvent(
                rental.getId(), book.getId(), renter.getId(), book.getOwner().getId()));
        log.info("Reserva aceita: id={} -> rentalId={}", reservationId, rental.getId());
        return mapper.toResponse(reservation, 0);
    }

    @Transactional
    public ReservationResponse decline(UUID userId, UUID reservationId) {
        Reservation reservation = loadOwned(userId, reservationId);
        boolean wasOffered = reservation.isOffered();
        reservation.decline();
        if (wasOffered) {
            offerNextOrRelease(reservation.getBook());
        }
        return mapper.toResponse(reservation, 0);
    }

    @Transactional
    public void leave(UUID userId, UUID reservationId) {
        Reservation reservation = loadOwned(userId, reservationId);
        if (!reservation.isWaiting()) {
            throw new ConflictException("Só é possível sair da fila enquanto aguardando.", "NOT_WAITING");
        }
        reservation.decline();
    }

    /**
     * Oferta o livro ao próximo da fila (segurando-o como RESERVED); se não houver
     * ninguém, libera o livro. Chamado pelos listeners de devolução/recusa (e, na
     * fatia 4b, pelo scheduler de expiração). Retorna true se ofertou a alguém.
     */
    @Transactional
    public boolean offerNextOrRelease(Book book) {
        return reservationRepository
                .findFirstByBookIdAndStatusOrderByCreatedAtAsc(book.getId(), ReservationStatus.WAITING)
                .map(next -> {
                    book.markReserved();
                    next.offer(Instant.now().plus(Reservation.OFFER_TTL_HOURS, ChronoUnit.HOURS));
                    notificationService.create(
                            next.getOrganizationId(),
                            next.getUser().getId(), null, NotificationType.RESERVATION_OFFERED,
                            "O livro está disponível para você!",
                            "\"" + book.getTitle() + "\" foi reservado para você. Você tem "
                                    + Reservation.OFFER_TTL_HOURS + "h para aceitar.",
                            "RESERVATION", next.getId(), "/app/reservations");
                    log.info("Oferta enviada: reservationId={} bookId={}", next.getId(), book.getId());
                    return true;
                })
                .orElseGet(() -> {
                    if (!book.isAvailable()) {
                        book.markReturned();   // fila esgotada -> livro volta a ficar disponível
                    }
                    return false;
                });
    }

    /**
     * Vigia por tempo (item 4b): expira ofertas cujo prazo passou, notifica o
     * interessado e passa a vez ao próximo da fila. Chamado pelo scheduler.
     */
    @Transactional
    public void expireStaleOffers() {
        List<Reservation> stale = reservationRepository
                .findByStatusAndOfferExpiresAtBefore(ReservationStatus.OFFERED, Instant.now());
        for (Reservation r : stale) {
            r.expire();
            notificationService.create(
                    r.getOrganizationId(),
                    r.getUser().getId(), null, NotificationType.RESERVATION_EXPIRED,
                    "Sua oferta de reserva expirou",
                    "O prazo para pegar \"" + r.getBook().getTitle()
                            + "\" acabou. Passamos a vez para o próximo da fila.",
                    "RESERVATION", r.getId(), "/app/reservations");
            offerNextOrRelease(r.getBook());
        }
        if (!stale.isEmpty()) {
            log.info("Ofertas de reserva expiradas: {}", stale.size());
        }
    }

    private int positionOf(Reservation r) {
        if (r.getStatus() != ReservationStatus.WAITING) {
            return 0;
        }
        return (int) reservationRepository.countByBookIdAndStatusAndCreatedAtBefore(
                r.getBook().getId(), ReservationStatus.WAITING, r.getCreatedAt()) + 1;
    }

    private Reservation loadOwned(UUID userId, UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservationId));
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("Esta reserva não pertence a você.");
        }
        return reservation;
    }
}

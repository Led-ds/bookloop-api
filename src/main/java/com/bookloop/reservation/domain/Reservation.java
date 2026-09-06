package com.bookloop.reservation.domain;

import com.bookloop.book.domain.Book;
import com.bookloop.shared.domain.TenantEntity;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Uma posição na fila de reserva de um livro. Regras que cruzam agregados
 * (ofertar ao próximo, liberar o livro) vivem no ReservationService; aqui ficam
 * as transições locais de estado da própria reserva.
 */
@Getter
@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservations_book", columnList = "book_id"),
        @Index(name = "idx_reservations_user", columnList = "user_id"),
        @Index(name = "idx_reservations_status", columnList = "status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends TenantEntity {

    /** Janela para o interessado aceitar a oferta antes de passar a vez. */
    public static final long OFFER_TTL_HOURS = 48;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ReservationStatus status = ReservationStatus.WAITING;

    @Column(name = "offer_expires_at")
    private Instant offerExpiresAt;

    private Reservation(Book book, User user) {
        assignOrganization(book.getOrganizationId());
        this.book = book;
        this.user = user;
        this.status = ReservationStatus.WAITING;
    }

    public static Reservation waiting(Book book, User user) {
        if (book.isOwnedBy(user.getId())) {
            throw new BusinessException("Você não pode reservar o seu próprio livro.");
        }
        return new Reservation(book, user);
    }

    public void offer(Instant expiresAt) {
        requireStatus(ReservationStatus.WAITING, "Só é possível ofertar uma reserva em espera.");
        this.status = ReservationStatus.OFFERED;
        this.offerExpiresAt = expiresAt;
    }

    public void accept() {
        requireStatus(ReservationStatus.OFFERED, "Só é possível aceitar uma oferta ativa.");
        if (isOfferExpired()) {
            throw new BusinessException("A oferta desta reserva expirou.");
        }
        this.status = ReservationStatus.ACCEPTED;
    }

    public void decline() {
        if (status != ReservationStatus.OFFERED && status != ReservationStatus.WAITING) {
            throw new BusinessException("Esta reserva não pode ser recusada.");
        }
        this.status = ReservationStatus.DECLINED;
    }

    public void expire() {
        requireStatus(ReservationStatus.OFFERED, "Apenas uma oferta ativa pode expirar.");
        this.status = ReservationStatus.EXPIRED;
    }

    public boolean isWaiting() { return status == ReservationStatus.WAITING; }
    public boolean isOffered() { return status == ReservationStatus.OFFERED; }

    public boolean isOfferExpired() {
        return offerExpiresAt != null && Instant.now().isAfter(offerExpiresAt);
    }

    private void requireStatus(ReservationStatus expected, String msg) {
        if (status != expected) {
            throw new BusinessException(msg);
        }
    }
}

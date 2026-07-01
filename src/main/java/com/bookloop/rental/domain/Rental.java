package com.bookloop.rental.domain;

import com.bookloop.book.domain.Book;
import com.bookloop.shared.domain.BaseEntity;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root for the lending lifecycle. All transitions live here and guard
 * the invariants — services orchestrate, they never set status directly.
 *
 * pending → approved → active → returned
 *      ↘ rejected / cancelled        ↘ (overdue) late
 */
@Getter
@Entity
@Table(name = "rentals", indexes = {
        @Index(name = "idx_rentals_renter", columnList = "renter_id"),
        @Index(name = "idx_rentals_owner", columnList = "owner_id"),
        @Index(name = "idx_rentals_book", columnList = "book_id"),
        @Index(name = "idx_rentals_status", columnList = "status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rental extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 500)
    private String message;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status = RentalStatus.PENDING;

    // --- Assinatura digital simples do Termo de Responsabilidade ---
    @Column(name = "term_accepted", nullable = false)
    private boolean termAccepted;

    @Column(name = "term_signed_at")
    private Instant termSignedAt;

    @Column(name = "term_signer_name", length = 120)
    private String termSignerName;

    private Rental(Book book, User renter, User owner, String message,
                   LocalDate startDate, LocalDate endDate, String signerName) {
        if (book.isOwnedBy(renter.getId())) {
            throw new BusinessException("Você não pode alugar o seu próprio livro.");
        }
        if (!book.isAvailable()) {
            throw new BusinessException("Este livro não está disponível para aluguel.");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("A data de devolução deve ser posterior à data de início.");
        }
        if (!renter.canRequestRentals()) {
            throw new BusinessException("Sua conta possui penalidades que impedem novas solicitações.");
        }
        this.book = book;
        this.renter = renter;
        this.owner = owner;
        this.message = message;
        this.startDate = startDate;
        this.endDate = endDate;
        // term signature captured at request time
        this.termAccepted = true;
        this.termSignedAt = Instant.now();
        this.termSignerName = signerName;
    }

    /** Request requires the renter to sign the responsibility term up front. */
    public static Rental request(Book book, User renter, User owner, String message,
                                 LocalDate startDate, LocalDate endDate,
                                 boolean termAccepted, String signerName) {
        if (!termAccepted) {
            throw new BusinessException("É necessário aceitar o Termo de Responsabilidade.");
        }
        return new Rental(book, renter, owner, message, startDate, endDate, signerName);
    }

    public void approve() {
        requireStatus(RentalStatus.PENDING, "Apenas solicitações pendentes podem ser aprovadas.");
        this.status = RentalStatus.APPROVED;
        book.markRented();
    }

    public void reject() {
        requireStatus(RentalStatus.PENDING, "Apenas solicitações pendentes podem ser rejeitadas.");
        this.status = RentalStatus.REJECTED;
    }

    public void cancel() {
        requireStatus(RentalStatus.PENDING, "Só é possível cancelar antes da aprovação.");
        this.status = RentalStatus.CANCELLED;
    }

    /** Owner confirms the physical handover. */
    public void activate() {
        requireStatus(RentalStatus.APPROVED, "O aluguel precisa estar aprovado para ser ativado.");
        this.status = RentalStatus.ACTIVE;
    }

    public boolean returnBook() {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.LATE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser devolvidos.");
        }
        boolean wasLate = status == RentalStatus.LATE || LocalDate.now().isAfter(endDate);
        this.returnDate = LocalDate.now();
        this.status = RentalStatus.RETURNED;
        book.markReturned();
        return wasLate;
    }

    /** Renewal extends the return date but is itself subject to owner approval. */
    public void renewUntil(LocalDate newEndDate) {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.LATE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser renovados.");
        }
        if (!newEndDate.isAfter(endDate)) {
            throw new BusinessException("A nova data deve ser posterior à data atual de devolução.");
        }
        this.endDate = newEndDate;
        if (status == RentalStatus.LATE) {
            this.status = RentalStatus.ACTIVE;
        }
    }

    /** Idempotent overdue detection, run by a scheduled sweep or on read. */
    public void markLateIfOverdue() {
        if (status == RentalStatus.ACTIVE && LocalDate.now().isAfter(endDate)) {
            this.status = RentalStatus.LATE;
        }
    }

    public boolean involvesOwner(UUID userId) {
        return owner.getId().equals(userId);
    }

    public boolean involvesRenter(UUID userId) {
        return renter.getId().equals(userId);
    }

    private void requireStatus(RentalStatus expected, String error) {
        if (this.status != expected) {
            throw new BusinessException(error);
        }
    }
}

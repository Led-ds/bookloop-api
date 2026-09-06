package com.bookloop.rental.domain;

import com.bookloop.book.domain.Book;
import com.bookloop.shared.domain.TenantEntity;
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
public class Rental extends TenantEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "renewal_status", length = 15)
    private RenewalStatus renewalStatus;

    @Column(name = "renewal_requested_until")
    private LocalDate renewalRequestedUntil;

    // --- Assinatura digital simples do Termo de Responsabilidade ---
    @Column(name = "term_accepted", nullable = false)
    private boolean termAccepted;

    @Column(name = "term_signed_at")
    private Instant termSignedAt;

    @Column(name = "term_signer_name", length = 120)
    private String termSignerName;

    private Rental(Book book, User renter, User owner, String message,
                   LocalDate startDate, LocalDate endDate, String signerName) {
        this(book, renter, owner, message, startDate, endDate, signerName, false);
    }

    private Rental(Book book, User renter, User owner, String message,
                   LocalDate startDate, LocalDate endDate, String signerName, boolean fromReservation) {
        if (book.isOwnedBy(renter.getId())) {
            throw new BusinessException("Você não pode alugar o seu próprio livro.");
        }
        if (!fromReservation && !book.isAvailable()) {
            throw new BusinessException("Este livro não está disponível para aluguel.");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("A data de devolução deve ser posterior à data de início.");
        }
        if (!renter.canRequestRentals()) {
            throw new BusinessException("Sua conta possui penalidades que impedem novas solicitações.");
        }
        assignOrganization(book.getOrganizationId());
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

    /**
     * Aluguel originado de uma reserva aceita: o livro já está segurado (RESERVED)
     * desde a oferta, então pula a checagem de disponibilidade. Fica PENDING para o
     * dono aprovar (opção B).
     */
    public static Rental fromReservation(Book book, User renter, User owner,
                                         LocalDate startDate, LocalDate endDate, String signerName) {
        return new Rental(book, renter, owner, null, startDate, endDate, signerName, true);
    }

    public void approve() {
        requireStatus(RentalStatus.PENDING, "Apenas solicitações pendentes podem ser aprovadas.");
        this.status = RentalStatus.APPROVED;
        book.markReserved();
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
        book.markRented();
    }

    /** Leitor solicita renovação propondo uma nova data (aguarda aprovação do dono). */
    public void requestRenewal(LocalDate newEndDate) {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.OVERDUE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser renovados.");
        }
        if (!newEndDate.isAfter(endDate)) {
            throw new BusinessException("A nova data deve ser posterior à data atual de devolução.");
        }
        this.renewalStatus = RenewalStatus.REQUESTED;
        this.renewalRequestedUntil = newEndDate;
    }

    /** Dono aprova a renovação pendente: estende a data e limpa o pedido. */
    public void approveRenewal() {
        if (renewalStatus != RenewalStatus.REQUESTED) {
            throw new BusinessException("Não há renovação pendente para aprovar.");
        }
        renewUntil(renewalRequestedUntil);
        this.renewalStatus = RenewalStatus.APPROVED;
    }

    /** Dono rejeita a renovação pendente. */
    public void rejectRenewal() {
        if (renewalStatus != RenewalStatus.REQUESTED) {
            throw new BusinessException("Não há renovação pendente para rejeitar.");
        }
        this.renewalStatus = RenewalStatus.REJECTED;
    }

    /** Leitor sinaliza a devolução; aguarda a confirmação do dono (handshake). */
    public void requestReturn() {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.OVERDUE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser devolvidos.");
        }
        this.status = RentalStatus.RETURN_REQUESTED;
    }

    /** Dono confirma o recebimento: encerra o aluguel e libera o livro. */
    public boolean confirmReturn() {
        if (status != RentalStatus.RETURN_REQUESTED) {
            throw new BusinessException("Não há devolução pendente de confirmação para este aluguel.");
        }
        boolean wasLate = LocalDate.now().isAfter(endDate);
        this.returnDate = LocalDate.now();
        this.status = RentalStatus.RETURNED;
        book.markReturned();
        return wasLate;
    }

    public boolean returnBook() {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.OVERDUE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser devolvidos.");
        }
        boolean wasLate = status == RentalStatus.OVERDUE || LocalDate.now().isAfter(endDate);
        this.returnDate = LocalDate.now();
        this.status = RentalStatus.RETURNED;
        book.markReturned();
        return wasLate;
    }

    /** Renewal extends the return date but is itself subject to owner approval. */
    public void renewUntil(LocalDate newEndDate) {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.OVERDUE) {
            throw new BusinessException("Apenas aluguéis ativos podem ser renovados.");
        }
        if (!newEndDate.isAfter(endDate)) {
            throw new BusinessException("A nova data deve ser posterior à data atual de devolução.");
        }
        this.endDate = newEndDate;
        if (status == RentalStatus.OVERDUE) {
            this.status = RentalStatus.ACTIVE;
        }
    }

    /** Idempotent overdue detection, run by a scheduled sweep or on read. */
    public void markLateIfOverdue() {
        if (status == RentalStatus.ACTIVE && LocalDate.now().isAfter(endDate)) {
            this.status = RentalStatus.OVERDUE;
        }
    }

    public boolean involvesOwner(UUID userId) {
        return owner.getId().equals(userId);
    }

    public boolean involvesRenter(UUID userId) {
        return renter.getId().equals(userId);
    }

    /** Encerra uma solicitação pendente não respondida pelo dono (vigia por tempo). */
    public void expireRequest() {
        requireStatus(RentalStatus.PENDING, "Apenas solicitações pendentes podem expirar.");
        this.status = RentalStatus.CANCELLED;
    }

    private void requireStatus(RentalStatus expected, String error) {
        if (this.status != expected) {
            throw new BusinessException(error);
        }
    }
}

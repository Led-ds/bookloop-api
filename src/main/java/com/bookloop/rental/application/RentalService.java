package com.bookloop.rental.application;

import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.RentalStatus;
import com.bookloop.rental.domain.events.BookRentedEvent;
import com.bookloop.rental.domain.events.BookReturnedEvent;
import com.bookloop.rental.domain.events.RentalRejectedEvent;
import com.bookloop.rental.domain.events.RentalRequestExpiredEvent;
import com.bookloop.rental.domain.events.ReturnRequestedEvent;
import com.bookloop.rental.domain.events.RentalRequestedEvent;
import com.bookloop.shared.application.PageResponse;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ConflictException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final ApplicationEventPublisher events;

    /** Estados que ocupam o livro: não pode haver dois simultâneos para o mesmo livro. */
    private static final java.util.List<RentalStatus> ACTIVE_STATUSES =
            java.util.List.of(RentalStatus.PENDING, RentalStatus.APPROVED, RentalStatus.ACTIVE, RentalStatus.RETURN_REQUESTED);

    /** Janela para o dono responder a uma solicitação antes de ela ser encerrada. */
    private static final long REQUEST_TTL_HOURS = 48;

    @Transactional
    public RentalResponse request(UUID renterId, CreateRentalRequest req) {
        Book book = bookRepository.findById(req.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro", req.bookId()));
        User renter = userRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", renterId));

        // Concorrência (item 6): no máximo 1 aluguel ativo por livro.
        // (a) rejeição amigável antes do insert, para o caso comum.
        if (rentalRepository.existsByBookIdAndStatusIn(book.getId(), ACTIVE_STATUSES)) {
            throw new ConflictException(
                    "Este livro já foi reservado por outra pessoa.", "BOOK_ALREADY_RESERVED");
        }

        Rental rental = Rental.request(book, renter, book.getOwner(), req.message(),
                req.startDate(), req.endDate(), req.termAccepted(), req.signerName());
        try {
            // saveAndFlush força a checagem do índice único agora (dentro do try);
            // (b) o índice único parcial é a garantia final contra a corrida verdadeira.
            rentalRepository.saveAndFlush(rental);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Este livro já foi reservado por outra pessoa.", "BOOK_ALREADY_RESERVED");
        }
        log.info("Aluguel solicitado: rentalId={} bookId={} renterId={}",
                rental.getId(), book.getId(), renterId);
        events.publishEvent(new RentalRequestedEvent(
                rental.getId(), book.getId(), renterId, book.getOwner().getId()));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse approve(UUID ownerId, UUID rentalId) {
        Rental rental = loadAsOwner(ownerId, rentalId);
        rental.approve();
        log.info("Aluguel aprovado: rentalId={} ownerId={}", rentalId, ownerId);
        events.publishEvent(new BookRentedEvent(
                rental.getId(), rental.getBook().getId(),
                rental.getRenter().getId(), rental.getOwner().getId()));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse reject(UUID ownerId, UUID rentalId) {
        Rental rental = loadAsOwner(ownerId, rentalId);
        rental.reject();
        log.info("Aluguel rejeitado: rentalId={} ownerId={}", rentalId, ownerId);
        events.publishEvent(new RentalRejectedEvent(
                rental.getId(), rental.getBook().getId(), rental.getRenter().getId(), ownerId));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse activate(UUID ownerId, UUID rentalId) {
        Rental rental = loadAsOwner(ownerId, rentalId);
        rental.activate();
        log.info("Aluguel ativado (retirada): rentalId={} ownerId={}", rentalId, ownerId);
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse cancel(UUID renterId, UUID rentalId) {
        Rental rental = loadAsRenter(renterId, rentalId);
        rental.cancel();
        log.info("Aluguel cancelado: rentalId={} renterId={}", rentalId, renterId);
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    @Transactional
    public RentalResponse requestReturn(UUID renterId, UUID rentalId) {
        Rental rental = loadAsRenter(renterId, rentalId);
        rental.requestReturn();
        log.info("Devolução solicitada pelo leitor: rentalId={} renterId={}", rentalId, renterId);
        events.publishEvent(new ReturnRequestedEvent(
                rental.getId(), rental.getBook().getId(),
                rental.getRenter().getId(), rental.getOwner().getId()));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse confirmReturn(UUID ownerId, UUID rentalId) {
        Rental rental = loadAsOwner(ownerId, rentalId);
        boolean wasLate = rental.confirmReturn();
        if (wasLate) {
            rental.getRenter().addPenalty();
        }
        log.info("Devolução confirmada pelo dono: rentalId={} wasLate={}", rentalId, wasLate);
        events.publishEvent(new BookReturnedEvent(
                rental.getId(), rental.getBook().getId(), rental.getRenter().getId(), wasLate));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse returnBook(UUID userId, UUID rentalId) {
        Rental rental = load(rentalId);
        // both owner and renter may register the return
        if (!rental.involvesOwner(userId) && !rental.involvesRenter(userId)) {
            throw new ForbiddenOperationException("Você não participa deste aluguel.");
        }
        boolean wasLate = rental.returnBook();
        if (wasLate) {
            rental.getRenter().addPenalty();
        }
        log.info("Aluguel devolvido: rentalId={} wasLate={}", rentalId, wasLate);
        events.publishEvent(new BookReturnedEvent(
                rental.getId(), rental.getBook().getId(), rental.getRenter().getId(), wasLate));
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse renew(UUID ownerId, UUID rentalId, RenewRentalRequest req) {
        // renewal is subject to owner approval
        Rental rental = loadAsOwner(ownerId, rentalId);
        rental.renewUntil(req.newEndDate());
        return rentalMapper.toResponse(rental);
    }

    @Transactional
    public PageResponse<RentalResponse> myRentals(UUID renterId, Pageable pageable) {
        return PageResponse.from(rentalRepository.findByRenterId(renterId, pageable)
                .map(this::refreshAndMap));
    }

    @Transactional
    public PageResponse<RentalResponse> myLendings(UUID ownerId, Pageable pageable) {
        return PageResponse.from(rentalRepository.findByOwnerId(ownerId, pageable)
                .map(this::refreshAndMap));
    }

    /**
     * Detecta atraso na leitura e persiste a transição para LATE. Roda em
     * transação gravável para que a mudança de status seja efetivada (antes
     * estava em readOnly e a alteração se perdia ao final da transação).
     * Um job agendado seria o caminho ideal a médio prazo.
     */
    private RentalResponse refreshAndMap(Rental rental) {
        RentalStatus before = rental.getStatus();
        rental.markLateIfOverdue();
        if (rental.getStatus() != before) {
            rentalRepository.save(rental);
        }
        return rentalMapper.toResponse(rental);
    }

    private Rental load(UUID id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel", id));
    }

    private Rental loadAsOwner(UUID ownerId, UUID rentalId) {
        Rental rental = load(rentalId);
        if (!rental.involvesOwner(ownerId)) {
            throw new ForbiddenOperationException("Apenas o dono do livro pode executar esta ação.");
        }
        return rental;
    }

    private Rental loadAsRenter(UUID renterId, UUID rentalId) {
        Rental rental = load(rentalId);
        if (!rental.involvesRenter(renterId)) {
            throw new ForbiddenOperationException("Apenas o solicitante pode executar esta ação.");
        }
        return rental;
    }
    /**
     * Vigia por tempo (item 4b): encerra solicitações PENDING que o dono não
     * respondeu no prazo. Publica RentalRequestExpiredEvent, que os listeners
     * traduzem em notificações (solicitante e dono) e na liberação/oferta do livro.
     */
    @Transactional
    public void expireStalePendingRequests() {
        Instant cutoff = Instant.now().minus(REQUEST_TTL_HOURS, ChronoUnit.HOURS);
        List<Rental> stale = rentalRepository.findByStatusAndCreatedAtBefore(RentalStatus.PENDING, cutoff);
        for (Rental r : stale) {
            r.expireRequest();
            events.publishEvent(new RentalRequestExpiredEvent(
                    r.getId(), r.getBook().getId(), r.getRenter().getId(), r.getOwner().getId()));
        }
        if (!stale.isEmpty()) {
            log.info("Solicitações PENDING expiradas por inatividade: {}", stale.size());
        }
    }
}


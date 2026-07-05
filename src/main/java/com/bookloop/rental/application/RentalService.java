package com.bookloop.rental.application;

import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.RentalStatus;
import com.bookloop.rental.domain.events.BookRentedEvent;
import com.bookloop.rental.domain.events.BookReturnedEvent;
import com.bookloop.shared.application.PageResponse;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public RentalResponse request(UUID renterId, CreateRentalRequest req) {
        Book book = bookRepository.findById(req.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro", req.bookId()));
        User renter = userRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", renterId));

        Rental rental = Rental.request(book, renter, book.getOwner(), req.message(),
                req.startDate(), req.endDate(), req.termAccepted(), req.signerName());
        rentalRepository.save(rental);
        log.info("Aluguel solicitado: rentalId={} bookId={} renterId={}",
                rental.getId(), book.getId(), renterId);
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
}

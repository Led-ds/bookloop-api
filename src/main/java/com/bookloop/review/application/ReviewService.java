package com.bookloop.review.application;

import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.RentalStatus;
import com.bookloop.review.domain.Review;
import com.bookloop.review.domain.ReviewRepository;
import com.bookloop.review.domain.ReviewType;
import com.bookloop.shared.application.PageResponse;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Regras de avaliação: só após aluguel devolvido, 1 por aluguel por direção,
 * sem auto-avaliação, e recálculo denormalizado da média (livro/usuário).
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse create(UUID authorId, CreateReviewRequest req) {
        Rental rental = rentalRepository.findById(req.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel", req.rentalId()));

        if (rental.getStatus() != RentalStatus.RETURNED) {
            throw new BusinessException("Só é possível avaliar após a devolução do aluguel.");
        }

        boolean isRenter = rental.involvesRenter(authorId);
        boolean isOwner = rental.involvesOwner(authorId);
        if (!isRenter && !isOwner) {
            throw new ForbiddenOperationException("Você não participou deste aluguel.");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", authorId));

        Review review;
        if (req.type() == ReviewType.BOOK) {
            if (!isRenter) {
                throw new ForbiddenOperationException("Apenas quem alugou pode avaliar o livro.");
            }
            if (reviewRepository.existsByRentalIdAndAuthorIdAndReviewType(
                    rental.getId(), authorId, ReviewType.BOOK)) {
                throw new BusinessException("Você já avaliou este livro neste aluguel.");
            }
            Book book = rental.getBook();
            review = reviewRepository.save(Review.forBook(rental, author, book, req.rating(), req.comment()));
            recalcBook(book);
        } else {
            UUID targetId = req.targetUserId();
            if (targetId == null) {
                throw new BusinessException("Informe o usuário a ser avaliado.");
            }
            UUID expected = isRenter ? rental.getOwner().getId() : rental.getRenter().getId();
            if (!expected.equals(targetId)) {
                throw new BusinessException("Você só pode avaliar a contraparte deste aluguel.");
            }
            if (reviewRepository.existsByRentalIdAndAuthorIdAndTargetUserId(
                    rental.getId(), authorId, targetId)) {
                throw new BusinessException("Você já avaliou esta pessoa neste aluguel.");
            }
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", targetId));
            review = reviewRepository.save(Review.forUser(rental, author, target, req.rating(), req.comment()));
            recalcUser(target);
        }
        return reviewMapper.toResponse(review);
    }

    @Transactional
    public ReviewResponse update(UUID authorId, UUID reviewId, int rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação", reviewId));
        if (!review.getAuthor().getId().equals(authorId)) {
            throw new ForbiddenOperationException("Você só pode editar as suas próprias avaliações.");
        }
        review.edit(rating, comment);
        // Recalcula a média denormalizada do alvo (livro ou pessoa).
        if (review.getReviewType() == ReviewType.BOOK) {
            recalcBook(review.getTargetBook());
        } else {
            recalcUser(review.getTargetUser());
        }
        return reviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> bookReviews(UUID bookId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findByTargetBookIdOrderByCreatedAtDesc(bookId, pageable)
                        .map(reviewMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> userReviews(UUID userId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(reviewMapper::toResponse));
    }

    /** Aluguéis devolvidos em que o usuário ainda tem algo a avaliar. */
    @Transactional(readOnly = true)
    public List<PendingReviewResponse> pending(UUID userId) {
        List<Rental> returned = rentalRepository.findReturnedInvolving(userId);
        List<PendingReviewResponse> pending = new ArrayList<>();
        for (Rental rental : returned) {
            boolean isRenter = rental.involvesRenter(userId);
            boolean canReviewBook = isRenter && !reviewRepository
                    .existsByRentalIdAndAuthorIdAndReviewType(rental.getId(), userId, ReviewType.BOOK);
            User counterpart = isRenter ? rental.getOwner() : rental.getRenter();
            boolean canReviewUser = !reviewRepository
                    .existsByRentalIdAndAuthorIdAndTargetUserId(rental.getId(), userId, counterpart.getId());
            if (canReviewBook || canReviewUser) {
                pending.add(new PendingReviewResponse(
                        rental.getId(),
                        rental.getBook().getId(),
                        rental.getBook().getTitle(),
                        rental.getBook().getCoverUrl(),
                        counterpart.getId(),
                        counterpart.getName(),
                        canReviewBook,
                        canReviewUser));
            }
        }
        return pending;
    }

    private void recalcBook(Book book) {
        Double avg = reviewRepository.avgForBook(book.getId());
        long count = reviewRepository.countByTargetBookId(book.getId());
        book.applyRating(avg == null ? 0d : avg, count);
        bookRepository.save(book);
    }

    private void recalcUser(User user) {
        Double avg = reviewRepository.avgForUser(user.getId());
        long count = reviewRepository.countByTargetUserId(user.getId());
        user.applyRating(avg == null ? 0d : avg, count);
        userRepository.save(user);
    }
}

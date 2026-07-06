package com.bookloop.review.application;

import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.rental.domain.Rental;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.rental.domain.RentalStatus;
import com.bookloop.review.domain.Review;
import com.bookloop.review.domain.ReviewRepository;
import com.bookloop.review.domain.ReviewType;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;

    private final ReviewMapper reviewMapper = new ReviewMapper();
    private ReviewService service;

    private final UUID renterId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    private ReviewService service() {
        return new ReviewService(reviewRepository, rentalRepository, bookRepository, userRepository, reviewMapper);
    }

    private Rental returnedRental() {
        Rental rental = org.mockito.Mockito.mock(Rental.class);
        User renter = org.mockito.Mockito.mock(User.class);
        User owner = org.mockito.Mockito.mock(User.class);
        Book book = org.mockito.Mockito.mock(Book.class);
        lenient().when(renter.getId()).thenReturn(renterId);
        lenient().when(renter.getName()).thenReturn("Leitor");
        lenient().when(owner.getId()).thenReturn(ownerId);
        lenient().when(owner.getName()).thenReturn("Dono");
        lenient().when(book.getId()).thenReturn(bookId);
        lenient().when(book.getTitle()).thenReturn("O Hobbit");
        lenient().when(rental.getStatus()).thenReturn(RentalStatus.RETURNED);
        lenient().when(rental.getRenter()).thenReturn(renter);
        lenient().when(rental.getOwner()).thenReturn(owner);
        lenient().when(rental.getBook()).thenReturn(book);
        lenient().when(rental.getId()).thenReturn(UUID.randomUUID());
        lenient().when(rental.involvesRenter(renterId)).thenReturn(true);
        lenient().when(rental.involvesOwner(ownerId)).thenReturn(true);
        return rental;
    }

    @Test
    void cannotReviewWhenRentalNotReturned() {
        Rental rental = org.mockito.Mockito.mock(Rental.class);
        when(rental.getStatus()).thenReturn(RentalStatus.ACTIVE);
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.BOOK, null, 5, "bom");
        assertThrows(BusinessException.class, () -> service().create(renterId, req));
    }

    @Test
    void nonParticipantCannotReview() {
        Rental rental = returnedRental();
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.BOOK, null, 5, "bom");
        assertThrows(ForbiddenOperationException.class,
                () -> service().create(UUID.randomUUID(), req));
    }

    @Test
    void onlyRenterCanReviewBook() {
        Rental rental = returnedRental();
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        User owner = rental.getOwner();
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.BOOK, null, 5, "bom");
        assertThrows(ForbiddenOperationException.class, () -> service().create(ownerId, req));
    }

    @Test
    void duplicateBookReviewIsRejected() {
        Rental rental = returnedRental();
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        when(userRepository.findById(renterId)).thenReturn(Optional.of(rental.getRenter()));
        when(reviewRepository.existsByRentalIdAndAuthorIdAndReviewType(any(), any(), any()))
                .thenReturn(true);
        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.BOOK, null, 5, "bom");
        assertThrows(BusinessException.class, () -> service().create(renterId, req));
    }

    @Test
    void renterReviewsBookHappyPath() {
        Rental rental = returnedRental();
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        when(userRepository.findById(renterId)).thenReturn(Optional.of(rental.getRenter()));
        when(reviewRepository.existsByRentalIdAndAuthorIdAndReviewType(any(), any(), any()))
                .thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reviewRepository.avgForBook(any())).thenReturn(5.0);
        when(reviewRepository.countByTargetBookId(any())).thenReturn(1L);

        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.BOOK, null, 5, "Ótimo estado");
        ReviewResponse resp = service().create(renterId, req);

        assertEquals(5, resp.rating());
        assertEquals("BOOK", resp.targetType());
        verify(reviewRepository).save(any(Review.class));
        verify(bookRepository).save(rental.getBook());
    }

    @Test
    void userReviewMustTargetCounterpart() {
        Rental rental = returnedRental();
        when(rentalRepository.findById(any())).thenReturn(Optional.of(rental));
        when(userRepository.findById(renterId)).thenReturn(Optional.of(rental.getRenter()));
        // renter tenta avaliar a si mesmo em vez do dono
        var req = new CreateReviewRequest(UUID.randomUUID(), ReviewType.USER, renterId, 5, "eu");
        assertThrows(BusinessException.class, () -> service().create(renterId, req));
    }
}

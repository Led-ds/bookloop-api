package com.bookloop.home.application;

import com.bookloop.book.application.BookMapper;
import com.bookloop.book.application.BookSummaryResponse;
import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.book.domain.BookSpecifications;
import com.bookloop.rental.domain.RentalRepository;
import com.bookloop.review.application.ReviewMapper;
import com.bookloop.review.application.ReviewResponse;
import com.bookloop.review.application.TopReaderResponse;
import com.bookloop.review.domain.ReviewRepository;
import com.bookloop.review.domain.ReviewType;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final ReviewRepository reviewRepository;
    private final BookMapper bookMapper;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public PublicHomeResponse getHome() {
        Specification<Book> available = BookSpecifications.availableInCatalog();
        Specification<Book> featuredSpec = available.and(BookSpecifications.hasCover());
        var recent = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<BookSummaryResponse> featured = bookRepository.findAll(featuredSpec, recent)
                .map(bookMapper::toSummary).getContent();
        List<BookSummaryResponse> community = bookRepository
                .findAll(BookSpecifications.publiclyVisible(), recent)
                .map(bookMapper::toSummary).getContent();

        HomeStats stats = new HomeStats(
                bookRepository.count(),
                userRepository.count(),
                rentalRepository.count(),
                bookRepository.count(available),
                reviewRepository.overallAverage()   // média real (null enquanto não há avaliações)
        );

        List<RecentActivityResponse> activities = featured.stream()
                .limit(5)
                .map(b -> new RecentActivityResponse(
                        "BOOK_ADDED", "Novo livro na comunidade: " + b.title(), null))
                .toList();

        // "Vozes da comunidade": avaliações recentes de pessoas (relacional).
        List<ReviewResponse> reviews = reviewRepository
                .findTop6ByReviewTypeOrderByCreatedAtDesc(ReviewType.USER).stream()
                .map(reviewMapper::toResponse)
                .toList();

        // Leitores com melhor reputação.
        List<TopReaderResponse> topReaders = userRepository
                .findTop4ByRatingCountGreaterThanOrderByRatingAvgDescRatingCountDesc(0).stream()
                .map(u -> new TopReaderResponse(
                        u.getId(), u.getName(), u.getAvatarUrl(), u.getRatingAvg(), u.getRatingCount()))
                .toList();

        BookSummaryResponse bookOfTheWeek = featured.isEmpty() ? null : featured.get(0);

        return new PublicHomeResponse(
                stats, featured, community, activities, reviews, topReaders, bookOfTheWeek);
    }
}

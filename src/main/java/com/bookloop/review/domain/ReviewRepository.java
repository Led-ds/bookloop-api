package com.bookloop.review.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByRentalIdAndAuthorIdAndReviewType(UUID rentalId, UUID authorId, ReviewType reviewType);

    boolean existsByRentalIdAndAuthorIdAndTargetUserId(UUID rentalId, UUID authorId, UUID targetUserId);

    Page<Review> findByTargetBookIdOrderByCreatedAtDesc(UUID bookId, Pageable pageable);

    Page<Review> findByTargetUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Review> findTop6ByReviewTypeOrderByCreatedAtDesc(ReviewType reviewType);

    @Query("select avg(r.rating) from Review r where r.targetBook.id = :id")
    Double avgForBook(UUID id);

    long countByTargetBookId(UUID id);

    @Query("select avg(r.rating) from Review r where r.targetUser.id = :id")
    Double avgForUser(UUID id);

    long countByTargetUserId(UUID id);

    @Query("select avg(r.rating) from Review r")
    Double overallAverage();
}

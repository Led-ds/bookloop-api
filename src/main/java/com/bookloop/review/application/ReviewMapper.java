package com.bookloop.review.application;

import com.bookloop.review.domain.Review;
import com.bookloop.review.domain.ReviewType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        boolean isBook = r.getReviewType() == ReviewType.BOOK;
        UUID targetId = isBook ? r.getTargetBook().getId() : r.getTargetUser().getId();
        String targetName = isBook ? r.getTargetBook().getTitle() : r.getTargetUser().getName();
        var author = r.getAuthor();
        return new ReviewResponse(
                r.getId(),
                r.getRating(),
                r.getComment(),
                author.getId(),
                author.getName(),
                author.getAvatarUrl(),
                r.getReviewType().name(),
                targetId,
                targetName,
                r.getCreatedAt());
    }
}

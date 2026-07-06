package com.bookloop.review.domain;

import com.bookloop.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewTest {

    // O construtor valida nota e comentário ANTES de tocar nas referências,
    // então podemos exercitar as invariantes locais sem montar o grafo completo.

    @Test
    void ratingBelowRangeIsRejected() {
        assertThrows(BusinessException.class,
                () -> Review.forBook(null, null, null, 0, null));
    }

    @Test
    void ratingAboveRangeIsRejected() {
        assertThrows(BusinessException.class,
                () -> Review.forBook(null, null, null, 6, null));
    }

    @Test
    void commentLongerThanMaxIsRejected() {
        String tooLong = "x".repeat(Review.MAX_COMMENT + 1);
        assertThrows(BusinessException.class,
                () -> Review.forBook(null, null, null, 5, tooLong));
    }
}

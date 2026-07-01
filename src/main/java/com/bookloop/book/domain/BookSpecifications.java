package com.bookloop.book.domain;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Composable, type-safe query fragments — replaces brittle dynamic JPQL.
 */
public final class BookSpecifications {

    private BookSpecifications() {}

    /** Only books that are publicly visible in the catalog. */
    public static Specification<Book> publiclyVisible() {
        return (root, q, cb) -> cb.and(
                cb.isTrue(root.get("isPublic")),
                cb.notEqual(root.get("status"), BookStatus.UNAVAILABLE));
    }

    public static Specification<Book> titleOrAuthorContains(String term) {
        if (term == null || term.isBlank()) return null;
        String like = "%" + term.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("author")), like));
    }

    public static Specification<Book> hasGenre(Genre genre) {
        if (genre == null) return null;
        return (root, q, cb) -> cb.equal(root.get("genre"), genre);
    }

    public static Specification<Book> hasStatus(BookStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Book> ownedBy(UUID ownerId) {
        if (ownerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }
}

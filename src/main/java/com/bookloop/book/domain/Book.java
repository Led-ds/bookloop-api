package com.bookloop.book.domain;

import com.bookloop.shared.domain.BaseEntity;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Aggregate root for a shareable physical book. Availability transitions are
 * driven by rentals — the Book never sets itself RENTED from the outside,
 * it exposes intention-revealing methods that guard the invariants.
 */
@Getter
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_owner", columnList = "owner_id"),
        @Index(name = "idx_books_status", columnList = "status"),
        @Index(name = "idx_books_genre", columnList = "genre")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 160)
    private String author;

    @Column(length = 20)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Genre genre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_condition", nullable = false, length = 20)
    private BookCondition condition;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "rating_avg", nullable = false)
    private double ratingAvg = 0d;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    private Book(String title, String author, String isbn, Genre genre, String description,
                 BookCondition condition, String coverUrl, boolean isPublic, User owner) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.description = description;
        this.condition = condition;
        this.coverUrl = coverUrl;
        this.isPublic = isPublic;
        this.owner = owner;
    }

    public static Book create(String title, String author, String isbn, Genre genre, String description,
                              BookCondition condition, String coverUrl, boolean isPublic, User owner) {
        return new Book(title, author, isbn, genre, description, condition, coverUrl, isPublic, owner);
    }

    public void update(String title, String author, String isbn, Genre genre, String description,
                       BookCondition condition, String coverUrl, boolean isPublic) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.description = description;
        this.condition = condition;
        this.coverUrl = coverUrl;
        this.isPublic = isPublic;
    }

    public boolean isOwnedBy(UUID userId) {
        return owner.getId().equals(userId);
    }

    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE && isPublic;
    }

    /** Aprovação reserva o livro (aguardando retirada). */
    public void markReserved() {
        if (status == BookStatus.RENTED) {
            throw new BusinessException("Este livro já está alugado.");
        }
        if (status == BookStatus.RESERVED) {
            throw new BusinessException("Este livro já está reservado.");
        }
        this.status = BookStatus.RESERVED;
    }

    /** Retirada física efetiva o empréstimo. */
    public void markRented() {
        if (status == BookStatus.RENTED) {
            throw new BusinessException("Este livro já está alugado.");
        }
        this.status = BookStatus.RENTED;
    }

    /** Called when a rental is returned. */
    public void markReturned() {
        this.status = BookStatus.AVAILABLE;
    }

    public void changeVisibility(boolean hidden) {
        if (status == BookStatus.RENTED) {
            throw new BusinessException("Não é possível alterar a visibilidade de um livro alugado.");
        }
        this.isPublic = !hidden;
    }

    /** Livro RENTED não pode ter atributos críticos editados. */
    public boolean isRented() {
        return status == BookStatus.RENTED;
    }

    /** Atualiza a média denormalizada a partir do recálculo no serviço de avaliações. */
    public void applyRating(double avg, long count) {
        this.ratingAvg = avg;
        this.ratingCount = (int) count;
    }
}

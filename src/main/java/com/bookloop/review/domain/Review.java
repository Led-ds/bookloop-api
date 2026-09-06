package com.bookloop.review.domain;

import com.bookloop.book.domain.Book;
import com.bookloop.rental.domain.Rental;
import com.bookloop.shared.domain.TenantEntity;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Avaliação (1..5 estrelas + comentário curto) originada de um aluguel devolvido.
 * As regras de quem pode avaliar quem/quando vivem no ReviewService (cruzam agregados);
 * aqui ficam as invariantes locais (faixa da nota, tamanho do comentário, não auto-avaliação).
 */
@Getter
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_target_book", columnList = "target_book_id"),
        @Index(name = "idx_reviews_target_user", columnList = "target_user_id"),
        @Index(name = "idx_reviews_author", columnList = "author_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends TenantEntity {

    public static final int MAX_COMMENT = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 10)
    private ReviewType reviewType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_book_id")
    private Book targetBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(nullable = false)
    private int rating;

    @Column(length = MAX_COMMENT)
    private String comment;

    @Column(nullable = false)
    private boolean edited = false;

    /** Edição pelo autor: atualiza nota/comentário e marca como editada. */
    public void edit(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("A nota deve ser entre 1 e 5 estrelas.");
        }
        if (comment != null && comment.length() > MAX_COMMENT) {
            throw new BusinessException("O comentário deve ter no máximo " + MAX_COMMENT + " caracteres.");
        }
        this.rating = rating;
        this.comment = comment;
        this.edited = true;
    }

    private Review(Rental rental, User author, ReviewType type,
                   Book targetBook, User targetUser, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("A nota deve ser entre 1 e 5 estrelas.");
        }
        if (comment != null && comment.length() > MAX_COMMENT) {
            throw new BusinessException("O comentário deve ter no máximo " + MAX_COMMENT + " caracteres.");
        }
        assignOrganization(rental.getBook().getOrganizationId());
        this.rental = rental;
        this.author = author;
        this.reviewType = type;
        this.targetBook = targetBook;
        this.targetUser = targetUser;
        this.rating = rating;
        this.comment = comment;
    }

    /** Avaliação de um livro (feita por quem o alugou). */
    public static Review forBook(Rental rental, User author, Book book, int rating, String comment) {
        return new Review(rental, author, ReviewType.BOOK, book, null, rating, comment);
    }

    /** Avaliação de uma pessoa (contraparte do aluguel). Bloqueia auto-avaliação. */
    public static Review forUser(Rental rental, User author, User target, int rating, String comment) {
        if (author.getId().equals(target.getId())) {
            throw new BusinessException("Não é possível avaliar a si mesmo.");
        }
        return new Review(rental, author, ReviewType.USER, null, target, rating, comment);
    }
}

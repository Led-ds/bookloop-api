package com.bookloop.book.application;

import com.bookloop.book.domain.*;
import com.bookloop.shared.application.PageResponse;
import com.bookloop.shared.exception.ForbiddenOperationException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> search(String term, Genre genre, BookStatus status, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(BookSpecifications.publiclyVisible())
                .and(BookSpecifications.titleOrAuthorContains(term))
                .and(BookSpecifications.hasGenre(genre))
                .and(BookSpecifications.hasStatus(status));
        return PageResponse.from(bookRepository.findAll(spec, pageable).map(bookMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> listMine(UUID ownerId, Pageable pageable) {
        Specification<Book> spec = Specification.where(BookSpecifications.ownedBy(ownerId));
        return PageResponse.from(bookRepository.findAll(spec, pageable).map(bookMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public BookResponse getById(UUID id) {
        return bookMapper.toResponse(load(id));
    }

    @Transactional
    public BookResponse create(UUID ownerId, CreateBookRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", ownerId));
        Book book = Book.create(req.title(), req.author(), req.isbn(), req.genre(),
                req.description(), req.condition(), req.coverUrl(), req.isPublic(), owner);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(UUID ownerId, UUID bookId, UpdateBookRequest req) {
        Book book = loadOwned(ownerId, bookId);
        book.update(req.title(), req.author(), req.isbn(), req.genre(),
                req.description(), req.condition(), req.coverUrl(), req.isPublic());
        return bookMapper.toResponse(book);
    }

    @Transactional
    public void delete(UUID ownerId, UUID bookId) {
        Book book = loadOwned(ownerId, bookId);
        if (book.getStatus() == BookStatus.RENTED) {
            throw new ForbiddenOperationException("Não é possível remover um livro que está alugado.");
        }
        bookRepository.delete(book);
    }

    private Book load(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", id));
    }

    private Book loadOwned(UUID ownerId, UUID bookId) {
        Book book = load(bookId);
        if (!book.isOwnedBy(ownerId)) {
            throw new ForbiddenOperationException("Você não é o dono deste livro.");
        }
        return book;
    }
}

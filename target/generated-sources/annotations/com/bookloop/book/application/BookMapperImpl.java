package com.bookloop.book.application;

import com.bookloop.book.domain.Book;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-02T22:27:05-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Azul Systems, Inc.)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public BookResponse toResponse(Book book) {
        if ( book == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        String author = null;
        String isbn = null;
        String description = null;
        String coverUrl = null;
        Instant createdAt = null;

        id = book.getId();
        title = book.getTitle();
        author = book.getAuthor();
        isbn = book.getIsbn();
        description = book.getDescription();
        coverUrl = book.getCoverUrl();
        createdAt = book.getCreatedAt();

        boolean isPublic = book.isPublic();
        String genre = book.getGenre().name();
        String condition = book.getCondition().name();
        String status = book.getStatus().name();
        BookOwnerView owner = toOwnerView(book);

        BookResponse bookResponse = new BookResponse( id, title, author, isbn, genre, description, condition, coverUrl, isPublic, status, owner, createdAt );

        return bookResponse;
    }

    @Override
    public BookSummaryResponse toSummary(Book book) {
        if ( book == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        String author = null;
        String coverUrl = null;

        id = book.getId();
        title = book.getTitle();
        author = book.getAuthor();
        coverUrl = book.getCoverUrl();

        String genre = book.getGenre().name();
        String condition = book.getCondition().name();
        String status = book.getStatus().name();
        String ownerName = book.getOwner().getName();

        BookSummaryResponse bookSummaryResponse = new BookSummaryResponse( id, title, author, genre, condition, coverUrl, status, ownerName );

        return bookSummaryResponse;
    }
}

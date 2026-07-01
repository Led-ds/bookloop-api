package com.bookloop.book.application;

import com.bookloop.book.domain.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "isPublic", expression = "java(book.isPublic())")
    @Mapping(target = "genre", expression = "java(book.getGenre().name())")
    @Mapping(target = "condition", expression = "java(book.getCondition().name())")
    @Mapping(target = "status", expression = "java(book.getStatus().name())")
    @Mapping(target = "owner", expression = "java(toOwnerView(book))")
    BookResponse toResponse(Book book);

    @Mapping(target = "genre", expression = "java(book.getGenre().name())")
    @Mapping(target = "condition", expression = "java(book.getCondition().name())")
    @Mapping(target = "status", expression = "java(book.getStatus().name())")
    @Mapping(target = "ownerName", expression = "java(book.getOwner().getName())")
    BookSummaryResponse toSummary(Book book);

    default BookOwnerView toOwnerView(Book book) {
        var o = book.getOwner();
        return new BookOwnerView(o.getId(), o.getName(), o.getAvatarUrl(), o.getLocation(), o.getPenaltiesCount());
    }
}

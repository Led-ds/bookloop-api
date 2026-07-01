package com.bookloop.rental.application;

import com.bookloop.rental.domain.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {

    @Mapping(target = "bookId", expression = "java(r.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(r.getBook().getTitle())")
    @Mapping(target = "bookCoverUrl", expression = "java(r.getBook().getCoverUrl())")
    @Mapping(target = "renterId", expression = "java(r.getRenter().getId())")
    @Mapping(target = "renterName", expression = "java(r.getRenter().getName())")
    @Mapping(target = "ownerId", expression = "java(r.getOwner().getId())")
    @Mapping(target = "ownerName", expression = "java(r.getOwner().getName())")
    @Mapping(target = "status", expression = "java(r.getStatus().name())")
    RentalResponse toResponse(Rental r);
}

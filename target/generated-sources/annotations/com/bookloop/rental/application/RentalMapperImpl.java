package com.bookloop.rental.application;

import com.bookloop.rental.domain.Rental;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-03T09:12:30-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Azul Systems, Inc.)"
)
@Component
public class RentalMapperImpl implements RentalMapper {

    @Override
    public RentalResponse toResponse(Rental r) {
        if ( r == null ) {
            return null;
        }

        UUID id = null;
        String message = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate returnDate = null;
        boolean termAccepted = false;
        Instant termSignedAt = null;
        String termSignerName = null;
        Instant createdAt = null;

        id = r.getId();
        message = r.getMessage();
        startDate = r.getStartDate();
        endDate = r.getEndDate();
        returnDate = r.getReturnDate();
        termAccepted = r.isTermAccepted();
        termSignedAt = r.getTermSignedAt();
        termSignerName = r.getTermSignerName();
        createdAt = r.getCreatedAt();

        UUID bookId = r.getBook().getId();
        String bookTitle = r.getBook().getTitle();
        String bookCoverUrl = r.getBook().getCoverUrl();
        UUID renterId = r.getRenter().getId();
        String renterName = r.getRenter().getName();
        UUID ownerId = r.getOwner().getId();
        String ownerName = r.getOwner().getName();
        String status = r.getStatus().name();

        RentalResponse rentalResponse = new RentalResponse( id, bookId, bookTitle, bookCoverUrl, renterId, renterName, ownerId, ownerName, message, startDate, endDate, returnDate, status, termAccepted, termSignedAt, termSignerName, createdAt );

        return rentalResponse;
    }
}

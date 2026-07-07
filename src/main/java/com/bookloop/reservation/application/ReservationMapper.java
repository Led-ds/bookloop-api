package com.bookloop.reservation.application;

import com.bookloop.reservation.domain.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation r, int position) {
        return new ReservationResponse(
                r.getId(),
                r.getBook().getId(),
                r.getBook().getTitle(),
                r.getBook().getCoverUrl(),
                r.getStatus().name(),
                position,
                r.getOfferExpiresAt());
    }
}

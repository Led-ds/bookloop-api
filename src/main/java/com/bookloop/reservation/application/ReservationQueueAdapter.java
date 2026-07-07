package com.bookloop.reservation.application;

import com.bookloop.rental.application.ReservationQueuePort;
import com.bookloop.reservation.domain.ReservationRepository;
import com.bookloop.reservation.domain.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationQueueAdapter implements ReservationQueuePort {

    private final ReservationRepository reservationRepository;

    @Override
    public boolean hasActiveQueue(UUID bookId) {
        return reservationRepository.existsByBookIdAndStatusIn(
                bookId, List.of(ReservationStatus.WAITING, ReservationStatus.OFFERED));
    }
}

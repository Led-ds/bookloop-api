package com.bookloop.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Reservation> findFirstByBookIdAndStatusOrderByCreatedAtAsc(UUID bookId, ReservationStatus status);

    boolean existsByBookIdAndUserIdAndStatusIn(UUID bookId, UUID userId, Collection<ReservationStatus> statuses);

    long countByBookIdAndStatusAndCreatedAtBefore(UUID bookId, ReservationStatus status, Instant createdAt);

    List<Reservation> findByStatusAndOfferExpiresAtBefore(ReservationStatus status, Instant instant);
}

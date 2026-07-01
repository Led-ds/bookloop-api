package com.bookloop.rental.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {
    Page<Rental> findByRenterId(UUID renterId, Pageable pageable);
    Page<Rental> findByOwnerId(UUID ownerId, Pageable pageable);
    boolean existsByBookIdAndStatusIn(UUID bookId, java.util.Collection<RentalStatus> statuses);
}

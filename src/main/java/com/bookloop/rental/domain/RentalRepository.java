package com.bookloop.rental.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {
    Page<Rental> findByRenterId(UUID renterId, Pageable pageable);
    Page<Rental> findByOwnerId(UUID ownerId, Pageable pageable);
    boolean existsByBookIdAndStatusIn(UUID bookId, java.util.Collection<RentalStatus> statuses);

    @Query("select r from Rental r where r.status = com.bookloop.rental.domain.RentalStatus.RETURNED "
            + "and (r.renter.id = :userId or r.owner.id = :userId) order by r.updatedAt desc")
    java.util.List<Rental> findReturnedInvolving(UUID userId);
}

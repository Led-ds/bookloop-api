package com.bookloop.organization.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByCode(String code);
    Optional<Organization> findByCode(String code);
}

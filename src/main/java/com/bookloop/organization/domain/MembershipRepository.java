package com.bookloop.organization.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    List<Membership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    long countByOrganizationIdAndStatus(UUID organizationId, MembershipStatus status);

    List<Membership> findByOrganizationIdAndStatus(UUID organizationId, MembershipStatus status);

    long countByOrganizationIdAndRoleAndStatus(UUID organizationId, Role role, MembershipStatus status);
}

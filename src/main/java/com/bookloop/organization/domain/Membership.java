package com.bookloop.organization.domain;

import com.bookloop.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Vínculo entre um usuário e uma comunidade, com o papel que ele exerce ALI.
 * O papel é por comunidade — a mesma pessoa pode ter papéis diferentes em
 * comunidades diferentes.
 */
@Getter
@Entity
@Table(name = "memberships", indexes = {
        @Index(name = "idx_memberships_org",  columnList = "organization_id"),
        @Index(name = "idx_memberships_user", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    private Membership(UUID userId, UUID organizationId, Role role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.status = MembershipStatus.ACTIVE;
    }

    /** Vínculo do dono ao criar a comunidade. */
    public static Membership owner(UUID userId, UUID organizationId) {
        return new Membership(userId, organizationId, Role.OWNER);
    }

    /** Vínculo de um membro comum (ex.: ao aceitar convite). */
    public static Membership member(UUID userId, UUID organizationId) {
        return new Membership(userId, organizationId, Role.MEMBER);
    }

    public void promoteToAdmin() { this.role = Role.ADMIN; }
    public void demoteToMember() { this.role = Role.MEMBER; }
    public void remove()         { this.status = MembershipStatus.REMOVED; }
    public boolean isActive()    { return this.status == MembershipStatus.ACTIVE; }
}

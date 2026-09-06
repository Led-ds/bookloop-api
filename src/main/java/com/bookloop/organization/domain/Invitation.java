package com.bookloop.organization.domain;

import com.bookloop.shared.domain.BaseEntity;
import com.bookloop.shared.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Convite para entrar numa comunidade. Pode ser direcionado a um e-mail ou aberto
 * (só o código/token). Expira em 7 dias. Não é TenantEntity: é consultado por token
 * no fluxo público de aceite, antes de haver contexto de comunidade.
 */
@Getter
@Entity
@Table(name = "invitations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {

    public static final long TTL_DAYS = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(length = 180)
    private String email;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    @Column(name = "invited_by", nullable = false, updatable = false)
    private UUID invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Invitation(UUID organizationId, String email, String token, Role role, UUID invitedBy) {
        this.organizationId = organizationId;
        this.email = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();
        this.token = token;
        this.role = role;
        this.invitedBy = invitedBy;
        this.status = InvitationStatus.PENDING;
        this.expiresAt = Instant.now().plus(TTL_DAYS, ChronoUnit.DAYS);
    }

    public static Invitation create(UUID organizationId, String email, String token, Role role, UUID invitedBy) {
        return new Invitation(organizationId, email, token, role, invitedBy);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING && !isExpired();
    }

    /** Marca como aceito. Falha se não estiver mais utilizável. */
    public void accept() {
        if (status != InvitationStatus.PENDING) {
            throw new BusinessException("Este convite não está mais disponível.");
        }
        if (isExpired()) {
            this.status = InvitationStatus.EXPIRED;
            throw new BusinessException("Este convite expirou.");
        }
        this.status = InvitationStatus.ACCEPTED;
    }

    public void cancel() {
        if (status != InvitationStatus.PENDING) {
            throw new BusinessException("Só é possível cancelar convites pendentes.");
        }
        this.status = InvitationStatus.CANCELLED;
    }
}

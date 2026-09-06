package com.bookloop.organization.domain;

import com.bookloop.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Uma comunidade privada de leitura (na UI: "comunidade"; no código/banco:
 * organization). Agrupa membros, livros, empréstimos e avaliações, isolados
 * das demais comunidades. O Owner é fixo na v2.0 (sem transferência).
 */
@Getter
@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_organizations_owner", columnList = "owner_user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan;

    @Column(name = "member_limit", nullable = false)
    private int memberLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationStatus status;

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private UUID ownerUserId;

    private Organization(String code, String name, String description, UUID ownerUserId) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.ownerUserId = ownerUserId;
        this.plan = Plan.STARTER;
        this.memberLimit = 20;
        this.status = OrganizationStatus.ACTIVE;
    }

    /**
     * Cria uma nova comunidade. Nome e descrição são obrigatórios (decisão de
     * escopo: comunidade nasce com dados mínimos). O código de entrada é gerado
     * fora e passado aqui.
     */
    public static Organization create(String code, String name, String description, UUID ownerUserId) {
        return new Organization(code, name, description, ownerUserId);
    }

    public void rename(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void changePlan(Plan plan, int memberLimit) {
        this.plan = plan;
        this.memberLimit = memberLimit;
    }

    public void suspend()   { this.status = OrganizationStatus.SUSPENDED; }
    public void reactivate(){ this.status = OrganizationStatus.ACTIVE; }
    public boolean isActive(){ return this.status == OrganizationStatus.ACTIVE; }
}

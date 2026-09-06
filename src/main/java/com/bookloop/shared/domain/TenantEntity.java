package com.bookloop.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Base para toda entidade que pertence a uma comunidade (multi-tenant).
 * Declara organization_id e um filtro do Hibernate que, quando ATIVADO na sessão,
 * injeta automaticamente "WHERE organization_id = :orgId" em todas as consultas —
 * garantindo isolamento sem depender de cada query lembrar de filtrar.
 *
 * O filtro é ativado por requisição (ver TenantFilterAspect / interceptor) e fica
 * DESLIGADO fora de requisição (ex.: schedulers), que então enxergam todas as
 * comunidades — comportamento correto para varreduras de manutenção.
 */
@Getter
@MappedSuperclass
@FilterDef(name = TenantEntity.TENANT_FILTER,
        parameters = @ParamDef(name = TenantEntity.TENANT_PARAM, type = UUID.class))
@Filter(name = TenantEntity.TENANT_FILTER,
        condition = "organization_id = :" + TenantEntity.TENANT_PARAM)
public abstract class TenantEntity extends BaseEntity {

    public static final String TENANT_FILTER = "tenantFilter";
    public static final String TENANT_PARAM  = "orgId";

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    /** Define a comunidade dona (usado na criação da entidade). */
    protected void assignOrganization(UUID organizationId) {
        this.organizationId = organizationId;
    }
}

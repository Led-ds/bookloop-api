package com.bookloop.shared.tenant;

import com.bookloop.shared.domain.TenantEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Ativa o filtro multi-tenant do Hibernate na sessão atual, ligando o isolamento
 * automático para a comunidade informada. Chamado pelo OrganizationInterceptor
 * após validar o membership.
 */
@Component
public class TenantFilterActivator {

    @PersistenceContext
    private EntityManager entityManager;

    public void enable(UUID organizationId) {
        entityManager.unwrap(Session.class)
                .enableFilter(TenantEntity.TENANT_FILTER)
                .setParameter(TenantEntity.TENANT_PARAM, organizationId);
    }

    public void disable() {
        entityManager.unwrap(Session.class).disableFilter(TenantEntity.TENANT_FILTER);
    }
}

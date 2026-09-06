package com.bookloop.organization.application;

import com.bookloop.organization.domain.Role;
import com.bookloop.shared.exception.ForbiddenOperationException;

import java.util.UUID;

/**
 * Contexto da comunidade em que a requisição atual está agindo. Populado pelo
 * OrganizationInterceptor a partir do {orgId} na rota, após validar que o usuário
 * autenticado é membro ativo. Vive na thread da requisição e é limpo ao final.
 */
public final class OrganizationContext {

    private record Ctx(UUID organizationId, Role role) {}

    private static final ThreadLocal<Ctx> HOLDER = new ThreadLocal<>();

    private OrganizationContext() {}

    public static void set(UUID organizationId, Role role) {
        HOLDER.set(new Ctx(organizationId, role));
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static boolean isPresent() {
        return HOLDER.get() != null;
    }

    /** Organização ativa. Lança se não houver contexto (erro de programação). */
    public static UUID id() {
        Ctx c = HOLDER.get();
        if (c == null) {
            throw new ForbiddenOperationException("Nenhuma comunidade no contexto da requisição.");
        }
        return c.organizationId();
    }

    /** Papel do usuário na organização ativa. */
    public static Role role() {
        Ctx c = HOLDER.get();
        if (c == null) {
            throw new ForbiddenOperationException("Nenhuma comunidade no contexto da requisição.");
        }
        return c.role();
    }

    /** Garante que o papel atual é um dos permitidos, senão 403. */
    public static void require(Role... allowed) {
        Role current = role();
        for (Role r : allowed) {
            if (r == current) return;
        }
        throw new ForbiddenOperationException("Ação não permitida para o seu papel nesta comunidade.");
    }
}

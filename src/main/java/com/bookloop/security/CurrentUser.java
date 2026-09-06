package com.bookloop.security;

import com.bookloop.shared.exception.ForbiddenOperationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/** Convenience accessor for the authenticated principal inside services/controllers. */
public final class CurrentUser {

    private CurrentUser() {}

    public static AppUserDetails details() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new ForbiddenOperationException("Nenhum usuário autenticado.");
        }
        return details;
    }

    /** Id do usuário autenticado, ou null se a requisição for anônima (rotas públicas). */
    public static UUID idOrNull() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        try {
            return id();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static UUID id() {
        return details().getId();
    }
}

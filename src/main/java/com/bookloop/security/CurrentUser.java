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

    public static UUID id() {
        return details().getId();
    }
}

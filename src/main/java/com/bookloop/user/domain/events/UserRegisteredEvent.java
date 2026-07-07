package com.bookloop.user.domain.events;

import java.util.UUID;

/** Domain event: ContaCriada. Dispara o e-mail de boas-vindas (assíncrono, pós-commit). */
public record UserRegisteredEvent(UUID userId, String email, String name) {}

package com.bookloop.organization.application;

import jakarta.validation.constraints.Size;

/**
 * Ao aceitar: se o usuário NÃO tem conta, envia name+password (cria conta).
 * Se já está autenticado, envia vazio (só cria a membership).
 */
public record AcceptInvitationRequest(
        @Size(max = 120) String name,
        @Size(min = 8, max = 100) String password
) {}

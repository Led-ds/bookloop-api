package com.bookloop.organization.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** email nulo/vazio = convite por código aberto (qualquer um com o link entra). */
public record CreateInvitationRequest(
        @Email @Size(max = 180) String email
) {}

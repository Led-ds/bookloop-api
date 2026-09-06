package com.bookloop.organization.application;

/** Dados públicos que o convidado vê ANTES de aceitar (sem expor nada sensível). */
public record InvitationPreview(
        String organizationName,
        String organizationDescription,
        String invitedByName,
        boolean valid,
        String reason
) {}

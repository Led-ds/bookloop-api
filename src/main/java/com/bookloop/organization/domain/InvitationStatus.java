package com.bookloop.organization.domain;

public enum InvitationStatus {
    PENDING,    // aguardando aceite
    ACCEPTED,   // já usado — virou membership
    CANCELLED,  // cancelado por quem convidou
    EXPIRED     // passou da validade
}

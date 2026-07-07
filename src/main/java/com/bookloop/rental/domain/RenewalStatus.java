package com.bookloop.rental.domain;

public enum RenewalStatus {
    REQUESTED,  // leitor pediu; aguardando o dono
    APPROVED,   // dono aprovou; data estendida
    REJECTED    // dono rejeitou
}

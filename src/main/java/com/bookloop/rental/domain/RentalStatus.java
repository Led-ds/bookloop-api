package com.bookloop.rental.domain;

public enum RentalStatus {
    PENDING,    // pendente — aguardando aprovação do dono
    APPROVED,   // aprovado — termo assinado, livro reservado
    ACTIVE,     // ativo — livro em posse do leitor
    RETURNED,   // devolvido
    LATE,       // atrasado — passou da data de devolução
    REJECTED,   // rejeitado pelo dono
    CANCELLED   // cancelado pelo leitor antes da aprovação
}

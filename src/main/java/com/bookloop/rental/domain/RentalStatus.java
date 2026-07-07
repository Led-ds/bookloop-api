package com.bookloop.rental.domain;

public enum RentalStatus {
    PENDING,    // aguardando resposta do dono
    APPROVED,   // dono aprovou, aguardando retirada
    ACTIVE,     // livro em posse do leitor
    RETURN_REQUESTED, // leitor marcou "devolvi"; aguardando confirmação do dono
    RETURNED,   // devolvido (estado final)
    OVERDUE,    // atrasado (passou da data de devolução)
    REJECTED,   // recusado pelo dono (estado final)
    CANCELLED   // cancelado pelo leitor antes da aprovação (mantido; ver relatório v1.2)
}

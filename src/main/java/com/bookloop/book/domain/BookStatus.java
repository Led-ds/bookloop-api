package com.bookloop.book.domain;

/** Ciclo de vida de negócio do livro (visibilidade é controlada por isPublic, à parte). */
public enum BookStatus {
    AVAILABLE,   // disponível para solicitação
    RESERVED,    // solicitação aprovada, aguardando retirada
    RENTED       // em posse do leitor
}

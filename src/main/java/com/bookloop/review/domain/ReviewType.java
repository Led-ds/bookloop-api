package com.bookloop.review.domain;

/** Direção da avaliação: sobre um livro alugado ou sobre a contraparte (pessoa) do aluguel. */
public enum ReviewType {
    BOOK,   // avaliação do livro (feita por quem alugou)
    USER    // avaliação da pessoa (dono avalia leitor, leitor avalia dono)
}

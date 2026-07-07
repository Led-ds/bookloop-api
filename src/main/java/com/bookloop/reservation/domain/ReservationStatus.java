package com.bookloop.reservation.domain;

public enum ReservationStatus {
    WAITING,   // na fila, aguardando o livro ficar disponível
    OFFERED,   // livro disponível e ofertado a este interessado (com prazo)
    ACCEPTED,  // aceitou a oferta (gerou uma solicitação de aluguel)
    DECLINED,  // recusou a oferta ou saiu da fila
    EXPIRED    // não respondeu no prazo; perdeu a vez
}

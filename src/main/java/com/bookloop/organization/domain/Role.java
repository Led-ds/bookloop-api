package com.bookloop.organization.domain;

/**
 * Papel de um usuário DENTRO de uma comunidade. É contextual: a mesma pessoa
 * pode ser OWNER numa comunidade e MEMBER em outra.
 */
public enum Role {
    OWNER,   // criador; administra plano/comunidade e promove admins
    ADMIN,   // gere membros e livros da comunidade
    MEMBER   // participa: cadastra os próprios livros, empresta, avalia
}

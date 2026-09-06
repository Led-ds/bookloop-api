package com.bookloop.organization.application;

import com.bookloop.book.application.CreateBookRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criação de comunidade exige dados mínimos + o primeiro livro (decisão de escopo:
 * comunidade não nasce vazia).
 */
public record CreateOrganizationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String description,
        @NotNull @Valid CreateBookRequest firstBook
) {}

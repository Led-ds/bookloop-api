package com.bookloop.book.api;

import com.bookloop.book.application.*;
import com.bookloop.book.domain.BookStatus;
import com.bookloop.book.domain.Genre;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.shared.application.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Books", description = "Catálogo, cadastro e gestão de livros")
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Buscar livros no catálogo público (paginado e filtrável)")
    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> search(
            @PathVariable UUID orgId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) BookStatus status,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(bookService.search(q, genre, status, pageable));
    }

    @Operation(summary = "Detalhe de um livro + dados do dono")
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getById(@PathVariable UUID orgId, @PathVariable UUID id) {
        return ApiResponse.ok(bookService.getById(id));
    }

    @Operation(summary = "Listar os livros do usuário autenticado")
    @GetMapping("/mine")
    public ApiResponse<PageResponse<BookSummaryResponse>> mine(
            @PathVariable UUID orgId,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(bookService.listMine(CurrentUser.id(), pageable));
    }

    @Operation(summary = "Cadastrar um novo livro")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> create(@PathVariable UUID orgId, @Valid @RequestBody CreateBookRequest req) {
        var created = bookService.create(CurrentUser.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Livro cadastrado."));
    }

    @Operation(summary = "Atualizar um livro (apenas o dono)")
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> update(@PathVariable UUID orgId, @PathVariable UUID id, @Valid @RequestBody UpdateBookRequest req) {
        return ApiResponse.ok(bookService.update(CurrentUser.id(), id, req), "Livro atualizado.");
    }

    @Operation(summary = "Remover um livro (apenas o dono)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID orgId, @PathVariable UUID id) {
        bookService.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}

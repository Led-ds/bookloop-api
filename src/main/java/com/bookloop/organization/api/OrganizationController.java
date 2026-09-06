package com.bookloop.organization.api;

import com.bookloop.organization.application.CreateOrganizationRequest;
import com.bookloop.organization.application.OrganizationResponse;
import com.bookloop.organization.application.OrganizationService;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Organizations", description = "Comunidades privadas de leitura")
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Criar uma comunidade (com nome, descrição e o primeiro livro)")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> create(@Valid @RequestBody CreateOrganizationRequest req) {
        OrganizationResponse created = organizationService.create(CurrentUser.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Comunidade criada."));
    }

    @Operation(summary = "Listar minhas comunidades")
    @GetMapping
    public ApiResponse<List<OrganizationResponse>> mine() {
        return ApiResponse.ok(organizationService.listMine(CurrentUser.id()));
    }

    @Operation(summary = "Detalhe de uma comunidade (somente membros). "
            + "Rota escopada: valida membership via OrganizationInterceptor.")
    @GetMapping("/orgs/{orgId}")
    public ApiResponse<OrganizationResponse> getScoped(@org.springframework.web.bind.annotation.PathVariable java.util.UUID orgId) {
        // O interceptor já validou o membership e populou o contexto.
        // orgId no path == OrganizationContext.id(); usamos o serviço para montar a resposta.
        return ApiResponse.ok(organizationService.getForCurrentMember());
    }
}

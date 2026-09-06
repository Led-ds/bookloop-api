package com.bookloop.organization.api;

import com.bookloop.organization.application.CreateInvitationRequest;
import com.bookloop.organization.application.InvitationResponse;
import com.bookloop.organization.application.InvitationService;
import com.bookloop.shared.application.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Invitations", description = "Convites para entrar na comunidade")
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(summary = "Gerar convite (por e-mail ou código aberto) — dono ou admin")
    @PostMapping
    public ResponseEntity<ApiResponse<InvitationResponse>> create(
            @PathVariable UUID orgId, @Valid @RequestBody CreateInvitationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(invitationService.invite(req), "Convite gerado."));
    }

    @Operation(summary = "Listar convites pendentes — dono ou admin")
    @GetMapping
    public ApiResponse<List<InvitationResponse>> pending(@PathVariable UUID orgId) {
        return ApiResponse.ok(invitationService.listPending());
    }

    @Operation(summary = "Cancelar convite pendente — dono ou admin")
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID orgId, @PathVariable UUID invitationId) {
        invitationService.cancel(invitationId);
        return ResponseEntity.noContent().build();
    }
}

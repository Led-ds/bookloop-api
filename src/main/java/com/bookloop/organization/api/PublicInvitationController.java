package com.bookloop.organization.api;

import com.bookloop.organization.application.AcceptInvitationRequest;
import com.bookloop.organization.application.InvitationPreview;
import com.bookloop.organization.application.InvitationService;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.user.application.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invitations", description = "Convites para entrar na comunidade")
@RestController
@RequestMapping("/api/v1")
public class PublicInvitationController {

    private final InvitationService invitationService;

    public PublicInvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(summary = "Prévia de um convite (público, sem login)")
    @GetMapping("/public/invitations/{token}")
    public ApiResponse<InvitationPreview> preview(@PathVariable String token) {
        return ApiResponse.ok(invitationService.preview(token));
    }

    @Operation(summary = "Aceitar convite. Novo usuário envia nome+senha; logado envia vazio.")
    @PostMapping("/invitations/{token}/accept")
    public ApiResponse<AuthResponse> accept(@PathVariable String token,
                                            @Valid @RequestBody(required = false) AcceptInvitationRequest req) {
        // CurrentUser.idOrNull(): se autenticado, usa a conta; senão, cria uma nova.
        return ApiResponse.ok(invitationService.accept(token, req, CurrentUser.idOrNull()),
                "Bem-vindo(a) à comunidade!");
    }
}

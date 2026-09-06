package com.bookloop.organization.api;

import com.bookloop.organization.application.MemberResponse;
import com.bookloop.organization.application.MembershipService;
import com.bookloop.shared.application.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Members", description = "Membros e papéis dentro de uma comunidade")
@RestController
@RequestMapping("/api/v1/orgs/{orgId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MembershipService membershipService;

    @Operation(summary = "Listar membros da comunidade")
    @GetMapping
    public ApiResponse<List<MemberResponse>> list(@PathVariable UUID orgId) {
        return ApiResponse.ok(membershipService.listMembers());
    }

    @Operation(summary = "Promover membro a admin (somente dono)")
    @PatchMapping("/{membershipId}/promote")
    public ApiResponse<MemberResponse> promote(@PathVariable UUID orgId, @PathVariable UUID membershipId) {
        return ApiResponse.ok(membershipService.promote(membershipId), "Membro promovido a admin.");
    }

    @Operation(summary = "Rebaixar admin a membro (somente dono)")
    @PatchMapping("/{membershipId}/demote")
    public ApiResponse<MemberResponse> demote(@PathVariable UUID orgId, @PathVariable UUID membershipId) {
        return ApiResponse.ok(membershipService.demote(membershipId), "Admin rebaixado a membro.");
    }

    @Operation(summary = "Remover membro da comunidade (dono ou admin)")
    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Void> remove(@PathVariable UUID orgId, @PathVariable UUID membershipId) {
        membershipService.remove(membershipId);
        return ResponseEntity.noContent().build();
    }
}

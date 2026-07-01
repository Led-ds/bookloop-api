package com.bookloop.user.api;

import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.user.application.UpdateProfileRequest;
import com.bookloop.user.application.UserResponse;
import com.bookloop.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "Perfil do usuário")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Dados do usuário autenticado")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getById(CurrentUser.id()));
    }

    @Operation(summary = "Perfil público de um usuário")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @Operation(summary = "Atualizar o próprio perfil")
    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(CurrentUser.id(), req), "Perfil atualizado.");
    }
}

package com.bookloop.home.api;

import com.bookloop.home.application.HomeService;
import com.bookloop.home.application.PublicHomeResponse;
import com.bookloop.shared.application.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public", description = "Vitrine pública (sem autenticação)")
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final HomeService homeService;

    @Operation(summary = "Dados agregados da Home pública")
    @GetMapping("/home")
    public ApiResponse<PublicHomeResponse> home() {
        return ApiResponse.ok(homeService.getHome());
    }
}

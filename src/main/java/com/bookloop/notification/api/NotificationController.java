package com.bookloop.notification.api;

import com.bookloop.notification.application.MarkNotificationReadResponse;
import com.bookloop.notification.application.NotificationCountResponse;
import com.bookloop.notification.application.NotificationResponse;
import com.bookloop.notification.application.NotificationService;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.application.ApiResponse;
import com.bookloop.shared.application.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications", description = "Notificações internas do usuário autenticado")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Listar minhas notificações (paginado, mais recentes primeiro)")
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(notificationService.listForUser(CurrentUser.id(), pageable));
    }

    @Operation(summary = "Listar minhas notificações não lidas")
    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> unread() {
        return ApiResponse.ok(notificationService.listUnread(CurrentUser.id()));
    }

    @Operation(summary = "Contar minhas notificações não lidas")
    @GetMapping("/unread/count")
    public ApiResponse<NotificationCountResponse> unreadCount() {
        return ApiResponse.ok(notificationService.countUnread(CurrentUser.id()));
    }

    @Operation(summary = "Marcar uma notificação como lida")
    @PatchMapping("/{id}/read")
    public ApiResponse<MarkNotificationReadResponse> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.markAsRead(CurrentUser.id(), id),
                "Notificação marcada como lida.");
    }

    @Operation(summary = "Marcar todas as minhas notificações como lidas")
    @PatchMapping("/read-all")
    public ApiResponse<NotificationCountResponse> markAllRead() {
        return ApiResponse.ok(notificationService.markAllAsRead(CurrentUser.id()),
                "Notificações marcadas como lidas.");
    }
}

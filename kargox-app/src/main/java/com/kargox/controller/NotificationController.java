package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.common.PageResponse;
import com.kargox.dto.notification.NotificationResponse;
import com.kargox.security.SecurityUser;
import com.kargox.service.NotificationService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification inbox")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> myNotifications(@AuthenticationPrincipal
                                                                                           SecurityUser user,
                                                                                           @RequestParam(defaultValue = "0")
                                                                                            int page,
                                                                                           @RequestParam(defaultValue = "20")
                                                                                            int size) {

        return ResponseEntity.ok(ApiResponse.ok(notificationService.getForUser(user.getId(), page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(@AuthenticationPrincipal SecurityUser user) {

        return ResponseEntity.ok(ApiResponse.ok(notificationService.unreadCount(user.getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@AuthenticationPrincipal
                                                      SecurityUser user,
                                                      @PathVariable
                                                      Long id) {

        notificationService.markRead(id, user.getId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

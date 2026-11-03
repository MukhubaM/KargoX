package com.kargox.service;

import com.kargox.dto.common.PageResponse;
import com.kargox.dto.notification.NotificationResponse;
import com.kargox.entity.NotificationType;

public interface NotificationService {

    void notify(Long userId, NotificationType type, String title, String message, Long relatedBookingId);
    PageResponse<NotificationResponse> getForUser(Long userId, int page, int size);
    long unreadCount(Long userId);
    void markRead(Long notificationId, Long userId);
}

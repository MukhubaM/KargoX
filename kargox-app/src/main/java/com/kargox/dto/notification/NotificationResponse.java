package com.kargox.dto.notification;

import com.kargox.entity.NotificationType;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedBookingId;
    private boolean read;
    private Instant createdAt;
}

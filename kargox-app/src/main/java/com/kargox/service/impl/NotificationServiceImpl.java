package com.kargox.service.impl;

import com.kargox.dto.common.PageResponse;
import com.kargox.dto.notification.NotificationResponse;
import com.kargox.entity.Notification;
import com.kargox.entity.NotificationType;
import com.kargox.entity.User;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.NotificationRepository;
import com.kargox.repository.UserRepository;
import com.kargox.service.EmailService;
import com.kargox.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @Override
    @Transactional
    public void notify(Long userId, NotificationType type, String title, String message, Long relatedBookingId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedBookingId(relatedBookingId);
        notificationRepository.save(notification);

        // Push in-app via WebSocket to a user topic
        messagingTemplate.convertAndSend("/topic/users/" + userId + "/notifications", toResponse(notification));

        emailService.sendBookingNotificationEmail(user.getEmail(), "KargoX - " + title, message);
    }

    @Override
    public PageResponse<NotificationResponse> getForUser(Long userId, int page, int size) {

        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    public long unreadCount(Long userId) {

        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {

        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setRelatedBookingId(n.getRelatedBookingId());
        r.setRead(n.isRead());
        r.setCreatedAt(n.getCreatedAt());

        return r;
    }
}

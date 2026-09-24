package com.employeemanagement.service;

import com.employeemanagement.dto.response.NotificationResponse;
import com.employeemanagement.entity.Notification;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.enums.NotificationType;
import com.employeemanagement.entity.enums.Role;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.NotificationMapper;
import com.employeemanagement.repository.NotificationRepository;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void notifyUser(Long userId, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .readStatus(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyAllHrAndAdmins(String message, NotificationType type) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.HR)
                .forEach(u -> notifyUser(u.getId(), message, type));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadStatusFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, Long requestingUserId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        if (!notification.getUser().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notification.setReadStatus(true);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }
}

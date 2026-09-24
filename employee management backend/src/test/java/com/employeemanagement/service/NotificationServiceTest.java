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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("john.doe").role(Role.EMPLOYEE).build();
    }

    @Test
    void notifyUser_success_savesNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyUser(1L, "Your leave was approved", NotificationType.LEAVE_APPROVED);

        verify(notificationRepository).save(argThat(n ->
                n.getMessage().equals("Your leave was approved") &&
                n.getType() == NotificationType.LEAVE_APPROVED &&
                !n.getReadStatus()));
    }

    @Test
    void notifyUser_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.notifyUser(99L, "hi", NotificationType.GENERAL))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void notifyAllHrAndAdmins_onlyNotifiesHrAndAdminUsers() {
        User admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        User hr = User.builder().id(3L).username("hr_manager").role(Role.HR).build();
        User employee = User.builder().id(4L).username("employee1").role(Role.EMPLOYEE).build();

        when(userRepository.findAll()).thenReturn(List.of(admin, hr, employee));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(hr));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyAllHrAndAdmins("New leave request", NotificationType.LEAVE_APPLIED);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(userRepository, never()).findById(4L);
    }

    @Test
    void markAsRead_ownedByRequestingUser_marksAsRead() {
        Notification notification = Notification.builder().id(1L).user(user).message("Test")
                .type(NotificationType.GENERAL).readStatus(false).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(
                NotificationResponse.builder().id(1L).readStatus(true).build());

        NotificationResponse response = notificationService.markAsRead(1L, 1L);

        assertThat(response.getReadStatus()).isTrue();
    }

    @Test
    void markAsRead_notOwnedByRequestingUser_throwsException() {
        Notification notification = Notification.builder().id(1L).user(user).message("Test")
                .type(NotificationType.GENERAL).readStatus(false).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getUnreadCount_returnsCountFromRepository() {
        when(notificationRepository.countByUser_IdAndReadStatusFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(5L);
    }
}

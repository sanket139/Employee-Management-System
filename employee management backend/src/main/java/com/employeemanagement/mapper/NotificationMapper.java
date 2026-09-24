package com.employeemanagement.mapper;

import com.employeemanagement.dto.response.NotificationResponse;
import com.employeemanagement.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .readStatus(n.getReadStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

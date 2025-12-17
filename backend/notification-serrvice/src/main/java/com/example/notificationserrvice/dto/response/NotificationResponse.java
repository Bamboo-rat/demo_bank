package com.example.notificationserrvice.dto.response;

import com.example.notificationserrvice.entity.enums.NotificationType;
import com.example.notificationserrvice.entity.enums.Priority;
import com.example.notificationserrvice.entity.enums.ReferenceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    
    private String id;
    private NotificationType type;
    private String title;
    private String content;
    private ReferenceType referenceType;
    private Long referenceId;
    private Priority priority;
    private LocalDateTime createdAt;
    
    // User-specific fields
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
}

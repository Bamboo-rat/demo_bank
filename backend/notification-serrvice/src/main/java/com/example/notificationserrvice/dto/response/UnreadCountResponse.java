package com.example.notificationserrvice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thống kê thông báo chưa đọc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadCountResponse {
    
    private long unreadCount;
}

package com.example.notificationserrvice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.notificationserrvice.dto.response.NotificationPageResponse;
import com.example.notificationserrvice.dto.response.NotificationResponse;
import com.example.notificationserrvice.dto.response.UnreadCountResponse;
import com.example.notificationserrvice.entity.enums.NotificationType;
import com.example.notificationserrvice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để quản lý notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs quản lý thông báo của khách hàng")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Lấy danh sách thông báo",
            description = "Lấy danh sách thông báo của khách hàng với pagination, sắp xếp theo thời gian gần nhất",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotifications(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo mỗi trang")
            @RequestParam(defaultValue = "20") int size) {
        
        String customerId = getCurrentCustomerId();
        log.info("Getting notifications for customer: {}, page: {}, size: {}", customerId, page, size);
        
        NotificationPageResponse response = notificationService.getCustomerNotifications(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Lấy danh sách thông báo theo loại",
            description = "Lấy danh sách thông báo của khách hàng theo loại thông báo",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotificationsByType(
            @Parameter(description = "Loại thông báo")
            @PathVariable NotificationType type,
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo mỗi trang")
            @RequestParam(defaultValue = "20") int size) {
        
        String customerId = getCurrentCustomerId();
        log.info("Getting notifications by type for customer: {}, type: {}, page: {}, size: {}", 
                customerId, type, page, size);
        
        NotificationPageResponse response = notificationService
                .getCustomerNotificationsByType(customerId, type, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Lấy chi tiết thông báo",
            description = "Lấy chi tiết một thông báo của khách hàng",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationDetail(
            @Parameter(description = "ID của thông báo")
            @PathVariable String notificationId) {
        
        String customerId = getCurrentCustomerId();
        log.info("Getting notification detail: {} for customer: {}", notificationId, customerId);
        
        NotificationResponse response = notificationService.getNotificationDetail(customerId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Đánh dấu thông báo đã đọc",
            description = "Đánh dấu một thông báo là đã đọc",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "ID của thông báo")
            @PathVariable String notificationId) {
        
        String customerId = getCurrentCustomerId();
        log.info("Marking notification as read: {} for customer: {}", notificationId, customerId);
        
        NotificationResponse response = notificationService.markAsRead(customerId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu thông báo là đã đọc", response));
    }

    @Operation(
            summary = "Đánh dấu tất cả thông báo đã đọc",
            description = "Đánh dấu tất cả thông báo của khách hàng là đã đọc",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        String customerId = getCurrentCustomerId();
        log.info("Marking all notifications as read for customer: {}", customerId);
        
        int count = notificationService.markAllAsRead(customerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đánh dấu " + count + " thông báo là đã đọc", count));
    }

    @Operation(
            summary = "Đếm số thông báo chưa đọc",
            description = "Đếm số lượng thông báo chưa đọc của khách hàng (không query full list để tối ưu performance)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        String customerId = getCurrentCustomerId();
        log.info("Getting unread notification count for customer: {}", customerId);
        
        long count = notificationService.countUnreadNotifications(customerId);
        UnreadCountResponse response = UnreadCountResponse.builder()
                .unreadCount(count)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy customer ID từ JWT token
     */
    private String getCurrentCustomerId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String customerIdStr = jwt.getClaim("customerId");
        if (customerIdStr == null) {
            customerIdStr = jwt.getSubject();
        }
        
        if (customerIdStr == null) {
            throw new RuntimeException("Customer ID not found in token");
        }
        
        return customerIdStr;
    }
}

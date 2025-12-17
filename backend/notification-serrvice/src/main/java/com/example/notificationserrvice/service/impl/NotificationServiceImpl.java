package com.example.notificationserrvice.service.impl;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.dto.response.NotificationPageResponse;
import com.example.notificationserrvice.dto.response.NotificationResponse;
import com.example.notificationserrvice.entity.Notification;
import com.example.notificationserrvice.entity.UserNotificationStatus;
import com.example.notificationserrvice.entity.enums.DeliveryChannel;
import com.example.notificationserrvice.entity.enums.NotificationType;
import com.example.notificationserrvice.exception.NotificationNotFoundException;
import com.example.notificationserrvice.mapper.NotificationMapper;
import com.example.notificationserrvice.repository.NotificationRepository;
import com.example.notificationserrvice.repository.UserNotificationStatusRepository;
import com.example.notificationserrvice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public List<Notification> saveTransactionNotifications(TransactionNotificationEvent event) {
        log.info("Saving transaction notifications for transaction: {}", event.getTransactionReference());
        
        List<Notification> savedNotifications = new ArrayList<>();

        try {
            // 1. Tạo và lưu notification cho người gửi
            Notification senderNotification = notificationMapper.toSenderNotification(event);
            Notification savedSenderNotification = notificationRepository.save(senderNotification);
            savedNotifications.add(savedSenderNotification);
            
            // Tạo UserNotificationStatus cho người gửi
            createUserNotificationStatus(savedSenderNotification, Long.parseLong(event.getSenderCustomerId()));
            
            log.info("Saved sender notification: {} for customer: {}", 
                    savedSenderNotification.getId(), event.getSenderCustomerId());

            // 2. Tạo và lưu notification cho người nhận (chỉ với internal transfer)
            if (event.isInternalTransfer()) {
                Notification receiverNotification = notificationMapper.toReceiverNotification(event);
                Notification savedReceiverNotification = notificationRepository.save(receiverNotification);
                savedNotifications.add(savedReceiverNotification);
                
                // Tạo UserNotificationStatus cho người nhận
                createUserNotificationStatus(savedReceiverNotification, Long.parseLong(event.getReceiverCustomerId()));
                
                log.info("Saved receiver notification: {} for customer: {}", 
                        savedReceiverNotification.getId(), event.getReceiverCustomerId());
            }

            log.info("Successfully saved {} notification(s) for transaction: {}", 
                    savedNotifications.size(), event.getTransactionReference());
            
            return savedNotifications;

        } catch (Exception e) {
            log.error("Error saving transaction notifications for transaction: {}, Error: {}", 
                    event.getTransactionReference(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tạo UserNotificationStatus cho notification
     */
    private void createUserNotificationStatus(Notification notification, Long customerId) {
        UserNotificationStatus status = UserNotificationStatus.builder()
                .notification(notification)
                .customerId(customerId)
                .isRead(false)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .build();
        
        userNotificationStatusRepository.save(status);
        log.debug("Created UserNotificationStatus for notification: {}, customer: {}", 
                notification.getId(), customerId);
    }

    @Override
    @Transactional
    public Notification saveNotification(Notification notification) {
        log.info("Saving notification: type={}, referenceType={}, referenceId={}", 
                notification.getType(), notification.getReferenceType(), notification.getReferenceId());
        
        try {
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification saved successfully with ID: {}", savedNotification.getId());
            return savedNotification;
        } catch (Exception e) {
            log.error("Error saving notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getCustomerNotifications(Long customerId, int page, int size) {
        log.info("Getting notifications for customer: {}, page: {}, size: {}", customerId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserNotificationStatus> statusPage = userNotificationStatusRepository
                .findByCustomerIdAndChannel(customerId, DeliveryChannel.IN_APP, pageable);

        return buildPageResponse(statusPage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getCustomerNotificationsByType(
            Long customerId, NotificationType type, int page, int size) {
        log.info("Getting notifications for customer: {}, type: {}, page: {}, size: {}", 
                customerId, type, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserNotificationStatus> statusPage = userNotificationStatusRepository
                .findByCustomerIdAndChannelAndType(customerId, DeliveryChannel.IN_APP, type, pageable);

        return buildPageResponse(statusPage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationDetail(Long customerId, String notificationId) {
        log.info("Getting notification detail: {} for customer: {}", notificationId, customerId);
        
        UserNotificationStatus status = userNotificationStatusRepository
                .findByNotificationIdAndCustomerIdAndChannel(notificationId, customerId, DeliveryChannel.IN_APP)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId, customerId));

        return toNotificationResponse(status);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long customerId, String notificationId) {
        log.info("Marking notification as read: {} for customer: {}", notificationId, customerId);
        
        UserNotificationStatus status = userNotificationStatusRepository
                .findByNotificationIdAndCustomerIdAndChannel(notificationId, customerId, DeliveryChannel.IN_APP)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId, customerId));

        if (!status.getIsRead()) {
            status.markAsRead();
            status = userNotificationStatusRepository.save(status);
            log.info("Notification marked as read: {}", notificationId);
        } else {
            log.info("Notification already read: {}", notificationId);
        }

        return toNotificationResponse(status);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long customerId) {
        log.info("Marking all notifications as read for customer: {}", customerId);
        
        int count = userNotificationStatusRepository
                .markAllAsReadByCustomerIdAndChannel(customerId, DeliveryChannel.IN_APP);
        
        log.info("Marked {} notifications as read for customer: {}", count, customerId);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long customerId) {
        log.info("Counting unread notifications for customer: {}", customerId);
        
        long count = userNotificationStatusRepository
                .countUnreadByCustomerIdAndChannel(customerId, DeliveryChannel.IN_APP);
        
        log.info("Customer {} has {} unread notifications", customerId, count);
        return count;
    }

    /**
     * Build notification page response from UserNotificationStatus page
     */
    private NotificationPageResponse buildPageResponse(Page<UserNotificationStatus> statusPage) {
        List<NotificationResponse> notifications = statusPage.getContent().stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return NotificationPageResponse.builder()
                .notifications(notifications)
                .totalElements(statusPage.getTotalElements())
                .totalPages(statusPage.getTotalPages())
                .currentPage(statusPage.getNumber())
                .pageSize(statusPage.getSize())
                .hasNext(statusPage.hasNext())
                .hasPrevious(statusPage.hasPrevious())
                .build();
    }

    /**
     * Convert UserNotificationStatus to NotificationResponse
     */
    private NotificationResponse toNotificationResponse(UserNotificationStatus status) {
        Notification notification = status.getNotification();
        
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .priority(notification.getPriority())
                .createdAt(notification.getCreatedAt())
                .isRead(status.getIsRead())
                .readAt(status.getReadAt())
                .deliveredAt(status.getDeliveredAt())
                .build();
    }
}

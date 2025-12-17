package com.example.notificationserrvice.repository;

import com.example.notificationserrvice.entity.UserNotificationStatus;
import com.example.notificationserrvice.entity.enums.DeliveryChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserNotificationStatus
 */
public interface UserNotificationStatusRepository extends JpaRepository<UserNotificationStatus, String> {

    /**
     * Lấy danh sách notification status của customer với pagination
     * Sắp xếp theo thời gian tạo notification giảm dần
     */
    @Query("SELECT uns FROM UserNotificationStatus uns " +
           "JOIN FETCH uns.notification n " +
           "WHERE uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel " +
           "ORDER BY n.createdAt DESC")
    Page<UserNotificationStatus> findByCustomerIdAndChannel(
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel,
            Pageable pageable);

    /**
     * Lấy danh sách notification status theo loại thông báo
     */
    @Query("SELECT uns FROM UserNotificationStatus uns " +
           "JOIN FETCH uns.notification n " +
           "WHERE uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel " +
           "AND n.type = :type " +
           "ORDER BY n.createdAt DESC")
    Page<UserNotificationStatus> findByCustomerIdAndChannelAndType(
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel,
            @Param("type") com.example.notificationserrvice.entity.enums.NotificationType type,
            Pageable pageable);

    /**
     * Đếm số thông báo chưa đọc của customer
     * Không query full list để tối ưu performance
     */
    @Query("SELECT COUNT(uns) FROM UserNotificationStatus uns " +
           "WHERE uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel " +
           "AND uns.isRead = false")
    long countUnreadByCustomerIdAndChannel(
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel);

    /**
     * Lấy notification status chi tiết
     */
    @Query("SELECT uns FROM UserNotificationStatus uns " +
           "JOIN FETCH uns.notification n " +
           "WHERE uns.id = :id AND uns.customerId = :customerId")
    Optional<UserNotificationStatus> findByIdAndCustomerId(
            @Param("id") String id,
            @Param("customerId") Long customerId);

    /**
     * Tìm notification status theo notification ID và customer ID
     */
    @Query("SELECT uns FROM UserNotificationStatus uns " +
           "WHERE uns.notification.id = :notificationId " +
           "AND uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel")
    Optional<UserNotificationStatus> findByNotificationIdAndCustomerIdAndChannel(
            @Param("notificationId") String notificationId,
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel);

    /**
     * Đánh dấu tất cả thông báo là đã đọc cho customer
     * Bulk update để tối ưu performance
     */
    @Modifying
    @Query("UPDATE UserNotificationStatus uns " +
           "SET uns.isRead = true, uns.readAt = CURRENT_TIMESTAMP " +
           "WHERE uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel " +
           "AND uns.isRead = false")
    int markAllAsReadByCustomerIdAndChannel(
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel);

    /**
     * Lấy danh sách notification IDs chưa đọc
     */
    @Query("SELECT uns.id FROM UserNotificationStatus uns " +
           "WHERE uns.customerId = :customerId " +
           "AND uns.deliveryChannel = :channel " +
           "AND uns.isRead = false")
    List<String> findUnreadIdsByCustomerIdAndChannel(
            @Param("customerId") Long customerId,
            @Param("channel") DeliveryChannel channel);
}

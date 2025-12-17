package com.example.notificationserrvice.repository;

import com.example.notificationserrvice.entity.Notification;
import com.example.notificationserrvice.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository cho Notification
 */
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Lấy danh sách notifications theo created_by (customer ID)
     */
    Page<Notification> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);

    /**
     * Lấy danh sách notifications theo type và created_by
     */
    Page<Notification> findByTypeAndCreatedByOrderByCreatedAtDesc(
            NotificationType type, 
            String createdBy, 
            Pageable pageable);

    /**
     * Tìm notification theo ID và created_by
     */
    Optional<Notification> findByIdAndCreatedBy(String id, String createdBy);

    /**
     * Đếm số notifications theo created_by
     */
    long countByCreatedBy(String createdBy);
}

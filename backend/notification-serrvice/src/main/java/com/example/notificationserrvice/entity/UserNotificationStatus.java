package com.example.notificationserrvice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.example.notificationserrvice.entity.enums.DeliveryChannel;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_status", indexes = {
    @Index(name = "idx_user_notif_customer", columnList = "customer_id"),
    @Index(name = "idx_user_notif_notification", columnList = "notification_id"),
    @Index(name = "idx_user_notif_read", columnList = "customer_id,is_read"),
    @Index(name = "idx_user_notif_delivered", columnList = "delivered_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_notification_customer_channel", 
                     columnNames = {"notification_id", "customer_id", "delivery_channel"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationStatus {
    
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_user_notif_notification"))
    private Notification notification;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false, length = 20)
    private DeliveryChannel deliveryChannel;

    @CreationTimestamp
    @Column(name = "delivered_at", nullable = false, updatable = false)
    private LocalDateTime deliveredAt;

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}

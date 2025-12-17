package com.example.notificationserrvice.mapper;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.entity.Notification;
import com.example.notificationserrvice.entity.enums.NotificationType;
import com.example.notificationserrvice.entity.enums.Priority;
import com.example.notificationserrvice.entity.enums.ReferenceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class NotificationMapper {

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    /**
     * Build Notification Entity cho người gửi từ TransactionNotificationEvent
     * 
     * @param event Transaction notification event
     * @return Notification entity cho người gửi
     */
    public Notification toSenderNotification(TransactionNotificationEvent event) {
        String title = buildSenderTitle(event);
        String content = buildSenderContent(event);
        Priority priority = determinePriority(event.getAmount());

        return Notification.builder()
                .type(NotificationType.BALANCE_CHANGE)
                .title(title)
                .content(content)
                .referenceType(ReferenceType.TRANSACTION)
                .referenceId(Long.parseLong(event.getTransactionId()))
                .priority(priority)
                .createdBy(event.getSenderCustomerId())
                .build();
    }

    /**
     * Build Notification Entity cho người nhận từ TransactionNotificationEvent
     * Chỉ áp dụng cho giao dịch nội bộ (internal transfer)
     * 
     * @param event Transaction notification event
     * @return Notification entity cho người nhận
     */
    public Notification toReceiverNotification(TransactionNotificationEvent event) {
        if (!event.isInternalTransfer()) {
            throw new IllegalArgumentException("Cannot create receiver notification for external transfer");
        }

        String title = buildReceiverTitle(event);
        String content = buildReceiverContent(event);
        Priority priority = determinePriority(event.getAmount());

        return Notification.builder()
                .type(NotificationType.BALANCE_CHANGE)
                .title(title)
                .content(content)
                .referenceType(ReferenceType.TRANSACTION)
                .referenceId(Long.parseLong(event.getTransactionId()))
                .priority(priority)
                .createdBy(event.getReceiverCustomerId())
                .build();
    }

    /**
     * Xây dựng tiêu đề notification cho người gửi
     */
    private String buildSenderTitle(TransactionNotificationEvent event) {
        if (event.isInternalTransfer()) {
            return "Chuyển tiền thành công";
        } else {
            return "Chuyển tiền liên ngân hàng thành công";
        }
    }

    /**
     * Xây dựng nội dung notification cho người gửi
     */
    private String buildSenderContent(TransactionNotificationEvent event) {
        StringBuilder content = new StringBuilder();
        
        content.append("Tài khoản của bạn đã được trừ ");
        content.append(formatCurrency(event.getAmount()));
        content.append(" ").append(event.getCurrency());
        
        if (event.isInternalTransfer()) {
            content.append(" để chuyển đến tài khoản ");
            content.append(event.getReceiverAccountNumber());
            content.append(" - ").append(event.getReceiverName());
        } else {
            content.append(" để chuyển đến tài khoản ");
            content.append(event.getReceiverAccountNumber());
            content.append(" tại ").append(event.getReceiverBankName());
            content.append(" - ").append(event.getReceiverName());
        }
        
        content.append(".\n\nMã giao dịch: ").append(event.getTransactionReference());
        
        if (event.getFee() != null && event.getFee().compareTo(BigDecimal.ZERO) > 0) {
            content.append("\nPhí giao dịch: ").append(formatCurrency(event.getFee()));
            content.append(" ").append(event.getCurrency());
        }
        
        content.append("\nSố dư khả dụng: ").append(formatCurrency(event.getSenderBalanceAfter()));
        content.append(" ").append(event.getCurrency());
        
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            content.append("\nNội dung: ").append(event.getDescription());
        }
        
        return content.toString();
    }

    /**
     * Xây dựng tiêu đề notification cho người nhận
     */
    private String buildReceiverTitle(TransactionNotificationEvent event) {
        return "Nhận tiền thành công";
    }

    /**
     * Xây dựng nội dung notification cho người nhận
     */
    private String buildReceiverContent(TransactionNotificationEvent event) {
        StringBuilder content = new StringBuilder();
        
        content.append("Tài khoản của bạn đã được cộng ");
        content.append(formatCurrency(event.getAmount()));
        content.append(" ").append(event.getCurrency());
        content.append(" từ tài khoản ");
        content.append(event.getSenderAccountNumber());
        content.append(" - ").append(event.getSenderName());
        
        content.append(".\n\nMã giao dịch: ").append(event.getTransactionReference());
        content.append("\nSố dư khả dụng: ").append(formatCurrency(event.getReceiverBalanceAfter()));
        content.append(" ").append(event.getCurrency());
        
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            content.append("\nNội dung: ").append(event.getDescription());
        }
        
        return content.toString();
    }

    /**
     * Xác định độ ưu tiên của notification dựa trên số tiền
     */
    private Priority determinePriority(BigDecimal amount) {
        if (amount == null) {
            return Priority.NORMAL;
        }

        // Số tiền >= 100 triệu VND -> HIGH priority
        if (amount.compareTo(new BigDecimal("100000000")) >= 0) {
            return Priority.HIGH;
        }
        
        // Số tiền >= 10 triệu VND -> NORMAL priority
        if (amount.compareTo(new BigDecimal("10000000")) >= 0) {
            return Priority.NORMAL;
        }
        
        // Số tiền < 10 triệu VND -> LOW priority
        return Priority.LOW;
    }

    /**
     * Format số tiền theo định dạng Việt Nam
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return CURRENCY_FORMATTER.format(amount).replace("₫", "").trim();
    }
}

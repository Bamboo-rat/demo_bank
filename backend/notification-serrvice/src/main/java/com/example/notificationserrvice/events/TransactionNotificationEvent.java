package com.example.notificationserrvice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event nhận từ Kafka khi có giao dịch chuyển tiền thành công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionNotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Transaction info
    private String transactionId;
    private String transactionReference;
    private String transactionType;
    
    // Sender info
    private String senderAccountNumber;
    private String senderCustomerId;
    private String senderName;
    private String senderEmail;
    
    // Receiver info
    private String receiverAccountNumber;
    private String receiverCustomerId;
    private String receiverName;
    private String receiverEmail;
    private String receiverBankCode;
    private String receiverBankName;
    
    // Transaction details
    private BigDecimal amount;
    private String currency;
    private String description;
    private BigDecimal senderBalanceAfter;
    private BigDecimal receiverBalanceAfter;
    
    // Metadata
    private LocalDateTime transactionTime;
    private String status;
    private BigDecimal fee;
    
    public boolean isInternalTransfer() {
        return receiverBankCode == null || receiverBankCode.isBlank();
    }
}

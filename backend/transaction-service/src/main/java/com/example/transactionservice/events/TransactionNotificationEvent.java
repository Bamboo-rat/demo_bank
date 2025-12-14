package com.example.transactionservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionNotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Transaction info
    private String transactionId;
    private String transactionReference;
    private String transactionType; // INTERNAL_TRANSFER, EXTERNAL_TRANSFER
    
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
    private String receiverBankCode; // null for internal transfer
    private String receiverBankName; // null for internal transfer
    
    // Transaction details
    private BigDecimal amount;
    private String currency; // VND, USD, EUR
    private String description;
    private BigDecimal senderBalanceAfter; // Số dư sau giao dịch của người gửi
    private BigDecimal receiverBalanceAfter; // Số dư sau giao dịch của người nhận (internal only)
    
    // Metadata
    private LocalDateTime transactionTime;
    private String status; // SUCCESS, FAILED
    private BigDecimal fee; // Phí giao dịch
    
    /**
     * Kiểm tra xem có phải giao dịch nội bộ (cùng ngân hàng) không
     */
    public boolean isInternalTransfer() {
        return receiverBankCode == null || receiverBankCode.isBlank();
    }
}

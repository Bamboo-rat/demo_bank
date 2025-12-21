package com.example.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from partner bank account verification
 * Local DTO for transaction-service (decoupled from core-banking-service)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerBankAccountResponse {

    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String bankName;
    private Boolean exists;
    private Boolean active;
    private String message;
}

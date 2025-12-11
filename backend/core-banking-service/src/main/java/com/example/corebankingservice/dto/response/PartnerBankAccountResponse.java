package com.example.corebankingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from partner bank for account verification
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

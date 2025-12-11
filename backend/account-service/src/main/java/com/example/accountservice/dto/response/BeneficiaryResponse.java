package com.example.accountservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {

    private String beneficiaryId;
    private String customerId;
    private String beneficiaryAccountNumber;
    private String beneficiaryName;
    private String bankCode;
    private String bankName;
    private String nickname;
    private String note;
    private Boolean isVerified;
    private Integer transferCount;
    private LocalDateTime lastTransferDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indicates if this is an internal bank transfer (same bank)
     */
    public boolean isInternalTransfer() {
        return bankCode == null || bankCode.isBlank();
    }

    /**
     * Get display name (nickname if available, otherwise beneficiary name)
     */
    public String getDisplayName() {
        return (nickname != null && !nickname.isBlank()) ? nickname : beneficiaryName;
    }
}

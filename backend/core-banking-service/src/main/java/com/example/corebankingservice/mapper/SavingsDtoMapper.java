package com.example.corebankingservice.mapper;

import com.example.corebankingservice.dto.request.SavingsAccountCreationRequest;
import com.example.corebankingservice.dto.request.SavingsWithdrawalRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between account-service DTOs and core-banking DTOs
 */
@Component
public class SavingsDtoMapper {

    /**
     * Map from account-service CoreBankSavingsCreateRequest to core-banking SavingsAccountCreationRequest
     */
    public SavingsAccountCreationRequest mapCreateRequest(
            String savingsAccountId,
            String customerId,
            String cifNumber,
            String sourceAccountNumber,
            java.math.BigDecimal principalAmount,
            java.math.BigDecimal interestRate,
            String tenor,
            Integer tenorMonths,
            String interestPaymentMethod,
            String autoRenewType,
            java.time.LocalDate startDate,
            java.time.LocalDate maturityDate,
            String description) {
        
        return SavingsAccountCreationRequest.builder()
                .savingsAccountId(savingsAccountId)
                .customerId(customerId)
                .cifNumber(cifNumber)
                .sourceAccountNumber(sourceAccountNumber)
                .principalAmount(principalAmount)
                .interestRate(interestRate)
                .tenor(tenor)
                .tenorMonths(tenorMonths)
                .interestPaymentMethod(interestPaymentMethod)
                .autoRenewType(autoRenewType)
                .startDate(startDate)
                .maturityDate(maturityDate)
                .description(description)
                .build();
    }

    /**
     * Map from account-service CoreBankSavingsWithdrawRequest to core-banking SavingsWithdrawalRequest
     */
    public SavingsWithdrawalRequest mapWithdrawRequest(
            String savingsAccountId,
            String sourceAccountNumber,
            java.math.BigDecimal principalAmount,
            java.math.BigDecimal interestAmount,
            java.math.BigDecimal penaltyAmount,
            java.math.BigDecimal totalAmount,
            java.time.LocalDateTime withdrawnAt,
            String reason) {
        
        return SavingsWithdrawalRequest.builder()
                .savingsAccountId(savingsAccountId)
                .sourceAccountNumber(sourceAccountNumber)
                .principalAmount(principalAmount)
                .interestAmount(interestAmount)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .withdrawnAt(withdrawnAt)
                .reason(reason)
                .build();
    }
}

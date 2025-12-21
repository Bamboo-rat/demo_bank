package com.example.transactionservice.mapper;

import com.example.transactionservice.dto.response.TransferResponseDTO;
import com.example.transactionservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransferMapper {

    /**
     * Map Transaction entity to TransferResponseDTO
     * @param transaction transaction entity
     * @return transfer response DTO
     */
    @Mapping(source = "sourceAccountId", target = "sourceAccountNumber")
    @Mapping(source = "destinationAccountId", target = "destinationAccountNumber")
    @Mapping(source = "destinationBankCode", target = "destinationBankCode")
    @Mapping(source = "transferType", target = "transferType")
    @Mapping(source = "fee", target = "fee")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(transaction))")
    @Mapping(source = "transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", expression = "java(getCompletedAt(transaction))")
    @Mapping(target = "destinationBankName", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "digitalOtpRequired", constant = "false")
    TransferResponseDTO toResponseDTO(Transaction transaction);

    /**
     * Map Transaction entity to TransferResponseDTO with custom message
     * @param transaction transaction entity
     * @param message custom message
     * @return transfer response DTO
     */
    @Mapping(source = "transaction.sourceAccountId", target = "sourceAccountNumber")
    @Mapping(source = "transaction.destinationAccountId", target = "destinationAccountNumber")
    @Mapping(source = "transaction.destinationBankCode", target = "destinationBankCode")
    @Mapping(source = "transaction.transferType", target = "transferType")
    @Mapping(source = "transaction.fee", target = "fee")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(transaction))")
    @Mapping(source = "transaction.transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", expression = "java(getCompletedAt(transaction))")
    @Mapping(target = "destinationBankName", ignore = true)
    @Mapping(source = "message", target = "message")
    @Mapping(target = "digitalOtpRequired", constant = "false")
    TransferResponseDTO toResponseDTOWithMessage(Transaction transaction, String message);

    /**
     * Build TransferResponseDTO for Digital OTP initiation response
     */
    @Mapping(source = "transaction.sourceAccountId", target = "sourceAccountNumber")
    @Mapping(source = "transaction.destinationAccountId", target = "destinationAccountNumber")
    @Mapping(source = "transaction.destinationBankCode", target = "destinationBankCode")
    @Mapping(source = "transaction.transferType", target = "transferType")
    @Mapping(source = "transaction.fee", target = "fee")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(transaction))")
    @Mapping(source = "transaction.transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "destinationBankName", ignore = true)
    @Mapping(source = "message", target = "message")
    @Mapping(target = "digitalOtpRequired", constant = "true")
    TransferResponseDTO toDigitalOtpResponse(Transaction transaction, String message);

    /**
     * Helper method to calculate total amount (amount + fee if paid by source)
     */
    default java.math.BigDecimal calculateTotalAmount(Transaction transaction) {
        if (transaction.getAmount() == null) {
            return java.math.BigDecimal.ZERO;
        }
        
        java.math.BigDecimal fee = transaction.getFee() != null ? transaction.getFee() : java.math.BigDecimal.ZERO;
        
        // If fee is paid by source account, add to total
        if (transaction.getFeePaymentMethod() != null && 
            transaction.getFeePaymentMethod().name().equals("SOURCE")) {
            return transaction.getAmount().add(fee);
        }
        
        // Otherwise, total = amount only (fee paid by destination)
        return transaction.getAmount();
    }

    /**
     * Helper method to determine completed time
     */
    default LocalDateTime getCompletedAt(Transaction transaction) {
        return transaction.getStatus().isTerminal() ? LocalDateTime.now() : null;
    }
}

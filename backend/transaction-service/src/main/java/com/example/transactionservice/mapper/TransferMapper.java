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
    @Mapping(source = "transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", expression = "java(getCompletedAt(transaction))")
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
    @Mapping(source = "transaction.transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", expression = "java(getCompletedAt(transaction))")
    @Mapping(source = "message", target = "message")
    @Mapping(target = "digitalOtpRequired", constant = "false")
    TransferResponseDTO toResponseDTOWithMessage(Transaction transaction, String message);

    /**
     * Build TransferResponseDTO for Digital OTP initiation response
     */
    @Mapping(source = "transaction.sourceAccountId", target = "sourceAccountNumber")
    @Mapping(source = "transaction.destinationAccountId", target = "destinationAccountNumber")
    @Mapping(source = "transaction.transactionDate", target = "createdAt")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(source = "message", target = "message")
    @Mapping(target = "digitalOtpRequired", constant = "true")
    TransferResponseDTO toDigitalOtpResponse(Transaction transaction, String message);

    /**
     * Helper method to determine completed time
     */
    default LocalDateTime getCompletedAt(Transaction transaction) {
        return transaction.getStatus().isTerminal() ? LocalDateTime.now() : null;
    }
}

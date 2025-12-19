package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.request.TransactionRecordRequest;
import com.example.corebankingservice.entity.Transaction;
import com.example.corebankingservice.entity.enums.Currency;
import com.example.corebankingservice.exception.ResourceNotFoundException;
import com.example.corebankingservice.repository.TransactionRepository;
import com.example.corebankingservice.service.TransactionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionRecordServiceImpl implements TransactionRecordService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction recordTransferTransaction(TransactionRecordRequest request) {
        log.info("Recording transfer transaction in Core Banking: traceId={}, source={}, dest={}, amount={}", 
                request.getTraceId(), 
                request.getSourceAccountId(), 
                request.getDestinationAccountId(), 
                request.getAmount());

        // Check for idempotency - prevent duplicate records
        Transaction existingTransaction = transactionRepository.findByTraceId(request.getTraceId())
                .orElse(null);
        
        if (existingTransaction != null) {
            log.warn("Transaction already recorded: traceId={}, transactionId={}", 
                    request.getTraceId(), existingTransaction.getTransactionId());
            return existingTransaction;
        }

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .amount(request.getAmount())
                .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                .currency(Currency.VND)
                .transactionType(request.getTransactionType())
                .status(request.getStatus())
                .traceId(request.getTraceId())
                .referenceNumber(request.getTraceId()) // Use traceId as reference
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                // Set accounting fields
                .debitAccount(request.getSourceAccountId())
                .creditAccount(request.getDestinationAccountId())
                .balanceBefore(request.getSourceBalanceBefore())
                .balanceAfter(request.getSourceBalanceAfter())
                .build();

        // Set completion date if transaction is completed
        if (request.getStatus().isSuccessful()) {
            transaction.setCompletedDate(LocalDateTime.now());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Transaction recorded successfully in Core Banking: transactionId={}, traceId={}", 
                savedTransaction.getTransactionId(), savedTransaction.getTraceId());

        return savedTransaction;
    }

    @Override
    public Transaction getTransactionByTraceId(String traceId) {
        return transactionRepository.findByTraceId(traceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with traceId: " + traceId));
    }
}

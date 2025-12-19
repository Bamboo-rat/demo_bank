package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.request.TransactionRecordRequest;
import com.example.corebankingservice.entity.Transaction;

/**
 * Service for recording transactions in Core Banking System
 * This ensures all financial transactions are properly logged in the system of record
 */
public interface TransactionRecordService {

    /**
     * Record a transfer transaction in Core Banking
     * This creates the official transaction record for audit and reconciliation purposes
     * 
     * @param request the transaction record request containing all transaction details
     * @return the saved transaction entity
     */
    Transaction recordTransferTransaction(TransactionRecordRequest request);

    /**
     * Get transaction by trace ID (reference number from external system)
     * 
     * @param traceId the trace/reference ID
     * @return transaction if found
     */
    Transaction getTransactionByTraceId(String traceId);
}

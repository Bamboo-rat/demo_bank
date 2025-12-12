package com.example.transactionservice.service;

import com.example.transactionservice.dto.request.TransferConfirmDTO;
import com.example.transactionservice.dto.request.TransferRequestDTO;
import com.example.transactionservice.dto.response.TransferResponseDTO;
import com.example.transactionservice.entity.Transaction;

import java.util.List;

/**
 * Transfer Service for managing money transfers
 */
public interface TransferService {

    /**
     * Step 1: Initiate transfer and generate OTP
     * Creates PENDING transaction and sends OTP
     * 
     * @param request transfer request
     * @return transfer response with OTP sent confirmation
     */
    TransferResponseDTO initiateTransfer(TransferRequestDTO request);

    /**
     * Step 2: Confirm transfer with OTP and execute
     * Validates OTP and executes the transfer
     * 
     * @param confirmDTO OTP confirmation
     * @return transfer response with execution result
     */
    TransferResponseDTO confirmTransfer(TransferConfirmDTO confirmDTO);

    /**
     * Get transaction details by ID
     * 
     * @param transactionId transaction identifier
     * @return transaction details
     */
    TransferResponseDTO getTransactionById(String transactionId);

    /**
     * Get transaction history for account
     * 
     * @param accountNumber account number
     * @param page page number
     * @param size page size
     * @return list of transactions
     */
    List<TransferResponseDTO> getTransactionHistory(String accountNumber, int page, int size);

    /**
     * Cancel pending transaction
     * 
     * @param transactionId transaction identifier
     * @return cancellation result
     */
    TransferResponseDTO cancelTransaction(String transactionId);
}

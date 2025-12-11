package com.example.transactionservice.service;

import com.example.transactionservice.dto.response.BankResponse;

import java.util.List;

public interface BankService {

    /**
     * Get all banks from VietQR API
     * @return list of all banks
     */
    List<BankResponse> getAllBanks();

    /**
     * Search banks by name (full name or short name)
     * @param searchTerm the search term
     * @return list of matching banks
     */
    List<BankResponse> searchBanks(String searchTerm);

    /**
     * Get bank by code
     * @param bankCode the bank code (e.g., ACB, VCB)
     * @return bank information
     */
    BankResponse getBankByCode(String bankCode);

    /**
     * Get bank by BIN (Bank Identification Number)
     * @param bin the bank BIN
     * @return bank information
     */
    BankResponse getBankByBin(String bin);
}

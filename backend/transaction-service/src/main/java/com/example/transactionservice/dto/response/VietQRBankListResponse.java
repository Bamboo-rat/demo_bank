package com.example.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * VietQR API response wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietQRBankListResponse {

    private String code;
    private String desc;
    private List<BankResponse> data;
}

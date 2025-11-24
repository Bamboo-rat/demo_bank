package com.example.accountservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for account list response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListResponse {

    private String customerId;

    private List<AccountResponse> accounts;

    private int totalCount;

    private long timestamp;
}

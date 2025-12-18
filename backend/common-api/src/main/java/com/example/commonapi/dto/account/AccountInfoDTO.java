package com.example.commonapi.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Account Info DTO for Dubbo RPC
 * Contains basic account information for transaction processing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String accountNumber;
    private String customerId; // UUID của khách hàng
    private String accountHolderName; // Tên chủ tài khoản
    private String accountType; // SAVINGS, CHECKING, etc.
    private String status; // ACTIVE, DORMANT, etc.
    private String bankName; // Tên ngân hàng
    private String bankCode; // Mã ngân hàng
    private String cifNumber; // Mã CIF
    private Boolean isActive; // Trạng thái có thể nhận tiền không
}

package com.example.accountservice.dto.response;

import com.example.accountservice.entity.enums.AccountStatus;
import com.example.accountservice.entity.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoDTO {
    
    private String accountNumber;
    private String accountHolderName; // Tên chủ tài khoản
    private AccountType accountType;
    private AccountStatus status;
    private String bankName; // Tên ngân hàng
    private String bankCode; // Mã ngân hàng

    private String cifNumber; // Mã CIF
    private Boolean isActive; // Trạng thái có thể nhận tiền không
}

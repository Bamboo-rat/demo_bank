package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountStatusHistoryResponse {

    private String historyId;
    private String accountNumber;
    private AccountStatus previousStatus;
    private AccountStatus currentStatus;
    private String reason;
    private String performedBy;
    private LocalDateTime changedAt;
}

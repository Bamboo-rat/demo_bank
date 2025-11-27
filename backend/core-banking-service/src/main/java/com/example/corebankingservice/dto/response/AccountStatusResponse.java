package com.example.corebankingservice.dto.response;

import com.example.corebankingservice.entity.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountStatusResponse {

    private String accountNumber;
    private AccountStatus status;
    private boolean amlFlag;
    private LocalDateTime updatedAt;
}

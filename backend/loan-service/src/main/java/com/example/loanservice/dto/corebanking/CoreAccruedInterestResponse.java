package com.example.loanservice.dto.corebanking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreAccruedInterestResponse {
    private String loanServiceRef;
    private BigDecimal accruedInterest;
    private Integer daysAccrued;
}

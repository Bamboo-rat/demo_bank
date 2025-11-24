package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @UuidGenerator
    private String loanId;

    private String cifId;

    @Column(name = "principal_amount")
    private BigDecimal principalAmount; // Số tiền vay gốc

    @Column(name = "interest_rate ")
    private Double interestRate; // Lãi suất

    private Integer term; // Kỳ hạn

    @Enumerated(EnumType.STRING)
    private LoanStatus status;
}

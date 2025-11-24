package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.AccountStatus;
import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.entity.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @UuidGenerator
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "cif_id")
    private String cifId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance;

    private Currency currency;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

}

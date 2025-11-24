package com.example.accountservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "checking_accounts")
@PrimaryKeyJoinColumn(name = "accountId")
@Data
@NoArgsConstructor
public class CheckingAccount extends Account{
}

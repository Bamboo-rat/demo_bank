package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.AccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, String> {

    List<AccountStatusHistory> findByAccountNumberOrderByChangedAtDesc(String accountNumber);
}

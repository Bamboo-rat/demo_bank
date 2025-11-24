package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, String> {
}

package com.example.corebankingservice.entity;

import com.example.corebankingservice.entity.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusHistory {

    @Id
    @UuidGenerator
    @Column(name = "history_id")
    private String historyId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private AccountStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private AccountStatus currentStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "performed_by")
    private String performedBy;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;
}

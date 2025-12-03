package com.example.accountservice.dto.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response DTO for Dubbo account sync
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSyncResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String accountId; // Local account ID after sync
}

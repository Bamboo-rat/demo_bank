package com.example.commonapi.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response payload for account metadata synchronization over Dubbo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSyncResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String accountId;
}

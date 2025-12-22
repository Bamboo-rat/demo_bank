package com.example.loanservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Application errors (APP-001 -> APP-099)
    APP_001("APP-001", "Loan application not found"),
    APP_002("APP-002", "Customer not found or inactive"),
    APP_003("APP-003", "Customer has existing pending application"),
    APP_004("APP-004", "Loan amount exceeds limit"),
    APP_005("APP-005", "Invalid loan term"),
    APP_006("APP-006", "Invalid interest rate"),
    APP_007("APP-007", "Application already processed"),
    APP_008("APP-008", "Application not in pending status"),
    
    // Account errors (ACC-001 -> ACC-099)
    ACC_001("ACC-001", "Loan account not found"),
    ACC_002("ACC-002", "Loan account already exists"),
    ACC_003("ACC-003", "Loan account not active"),
    ACC_004("ACC-004", "Loan account already closed"),
    ACC_005("ACC-005", "Outstanding principal must be zero to close"),
    
    // Schedule errors (SCH-001 -> SCH-099)
    SCH_001("SCH-001", "Repayment schedule not found"),
    SCH_002("SCH-002", "Schedule already generated"),
    SCH_003("SCH-003", "Invalid repayment method"),
    SCH_004("SCH-004", "Schedule calculation failed"),
    SCH_005("SCH-005", "Installment not found"),
    SCH_006("SCH-006", "Installment already paid"),
    SCH_007("SCH-007", "Installment not yet due"),
    
    // Payment errors (PAY-001 -> PAY-099)
    PAY_001("PAY-001", "Payment amount invalid"),
    PAY_002("PAY-001", "Insufficient balance for payment"),
    PAY_003("PAY-003", "No pending installments"),
    PAY_004("PAY-004", "Early settlement not allowed"),
    PAY_005("PAY-005", "Payment record not found"),
    
    // Core Banking integration errors (CORE-001 -> CORE-099)
    CORE_001("CORE-001", "Core Banking service unavailable"),
    CORE_002("CORE-002", "Loan account creation failed in Core"),
    CORE_003("CORE-003", "Disbursement failed in Core"),
    CORE_004("CORE-004", "Repayment failed in Core"),
    CORE_005("CORE-005", "Core Banking account not found"),
    CORE_006("CORE-006", "Invalid response from Core Banking"),
    
    // Customer service integration errors (CUST-001 -> CUST-099)
    CUST_001("CUST-001", "Customer service unavailable"),
    CUST_002("CUST-002", "Customer verification failed"),
    CUST_003("CUST-003", "Customer credit check failed"),
    
    // Notification errors (NOTIF-001 -> NOTIF-099)
    NOTIF_001("NOTIF-001", "Failed to send notification event"),
    
    // General errors (GEN-001 -> GEN-099)
    GEN_001("GEN-001", "Internal server error"),
    GEN_002("GEN-002", "Invalid request parameters"),
    GEN_003("GEN-003", "Unauthorized access"),
    GEN_004("GEN-004", "Resource not found"),
    GEN_005("GEN-005", "Concurrent modification detected");
    
    private final String code;
    private final String message;
}

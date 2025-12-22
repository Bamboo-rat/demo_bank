package com.example.corebankingservice.service;

import com.example.corebankingservice.dto.loan.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanService {

    /**
     * Sinh số tài khoản khoản vay (14 số)
     * Format: 40XXXXXXXXXXXX
     */
    String generateLoanNumber();

    /**
     * Tạo loan ledger (sổ vay)
     * Gọi sau khi Loan Service duyệt vay
     */
    LoanDisbursementResponse createLoanAccount(LoanDisbursementRequest request);

    /**
     * Giải ngân - Credit tiền vào tài khoản
     * Ghi ledger, chống giải ngân trùng
     */
    LoanDisbursementResponse disburseLoan(String loanServiceRef);

    /**
     * Thu nợ - Debit tiền từ tài khoản
     * Kiểm tra số dư, update dư nợ, ghi ledger
     */
    LoanRepaymentResponse repayLoan(LoanRepaymentRequest request);

    /**
     * Tính lãi phát sinh theo dư nợ
     * Tính đến ngày hiện tại hoặc ngày cụ thể
     */
    BigDecimal calculateAccruedInterest(String loanServiceRef, LocalDate asOfDate);

    /**
     * Lấy thông tin khoản vay (dư nợ, lãi phát sinh, tổng nghĩa vụ)
     */
    LoanInfoResponse getLoanInfo(String loanServiceRef);

    /**
     * Tất toán khoản vay
     * Thu toàn bộ dư nợ, dừng tính lãi, đóng loan
     */
    LoanRepaymentResponse closeLoan(String loanServiceRef, String accountId);
}

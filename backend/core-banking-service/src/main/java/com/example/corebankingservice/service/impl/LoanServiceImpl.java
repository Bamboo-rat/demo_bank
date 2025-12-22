package com.example.corebankingservice.service.impl;

import com.example.corebankingservice.dto.loan.*;
import com.example.corebankingservice.entity.Account;
import com.example.corebankingservice.entity.Loan;
import com.example.corebankingservice.entity.Transaction;
import com.example.corebankingservice.entity.enums.AccountType;
import com.example.corebankingservice.entity.enums.LoanStatus;
import com.example.corebankingservice.entity.enums.TransactionChannel;
import com.example.corebankingservice.entity.enums.TransactionStatus;
import com.example.corebankingservice.entity.enums.TransactionType;
import com.example.corebankingservice.exception.BusinessException;
import com.example.corebankingservice.mapper.LoanMapper;
import com.example.corebankingservice.repository.AccountRepository;
import com.example.corebankingservice.repository.LoanRepository;
import com.example.corebankingservice.repository.TransactionRepository;
import com.example.corebankingservice.service.AccountNumberGenerator;
import com.example.corebankingservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Core Banking Loan Service Implementation
 * Money-centric: giữ tiền, ghi sổ, trả số liệu chính xác
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private static final int DAYS_IN_YEAR = 365;

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LoanMapper loanMapper;

    @Override
    public String generateLoanNumber() {
        log.info("[LOAN-GEN-001] Generating loan account number");
        return accountNumberGenerator.generate(AccountType.LOAN);
    }

    @Override
    @Transactional
    public LoanDisbursementResponse createLoanAccount(LoanDisbursementRequest request) {
        log.info("[LOAN-CREATE-001] Creating loan account for loanServiceRef: {}", request.getLoanServiceRef());

        // 1. Validate: không tạo trùng
        if (loanRepository.findByLoanServiceRef(request.getLoanServiceRef()).isPresent()) {
            throw new BusinessException("Loan already exists with reference: " + request.getLoanServiceRef());
        }

        // 2. Validate account
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BusinessException("Account not found: " + request.getAccountId()));

        if (!account.getCifNumber().equals(request.getCifId())) {
            throw new BusinessException("Account does not belong to customer");
        }

        // 3. Tạo loan ledger
        Loan loan = Loan.builder()
                .loanServiceRef(request.getLoanServiceRef())
                .cifId(request.getCifId())
                .accountId(request.getAccountId())
                .disbursedAmount(request.getDisbursementAmount())
                .outstandingPrincipal(request.getDisbursementAmount())
                .totalInterestPaid(BigDecimal.ZERO)
                .totalPenaltyPaid(BigDecimal.ZERO)
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .disbursementDate(request.getDisbursementDate())
                .maturityDate(request.getMaturityDate())
                .status(LoanStatus.APPROVED) // Chưa giải ngân
                .notes(request.getNotes())
                .build();

        loan = loanRepository.save(loan);
        log.info("[LOAN-CREATE-002] Loan account created: {}", loan.getLoanId());

        return LoanDisbursementResponse.builder()
                .loanId(loan.getLoanId())
                .loanServiceRef(loan.getLoanServiceRef())
                .disbursedAmount(BigDecimal.ZERO) // Chưa giải ngân
                .status("LOAN_ACCOUNT_CREATED")
                .message("Loan account created successfully. Ready for disbursement.")
                .disbursementTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public LoanDisbursementResponse disburseLoan(String loanServiceRef) {
        log.info("[LOAN-DISBURSE-001] Disbursing loan: {}", loanServiceRef);

        // 1. Tìm loan
        Loan loan = loanRepository.findByLoanServiceRef(loanServiceRef)
                .orElseThrow(() -> new BusinessException("Loan not found: " + loanServiceRef));

        // 2. Chống giải ngân trùng
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            throw new BusinessException("Loan already disbursed");
        }

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessException("Loan is not in approved status");
        }

        // 3. Lấy account
        Account account = accountRepository.findById(loan.getAccountId())
                .orElseThrow(() -> new BusinessException("Account not found: " + loan.getAccountId()));

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(loan.getDisbursedAmount());

        // 4. Credit tiền vào tài khoản
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        // 5. Ghi ledger (transaction)
        Transaction transaction = Transaction.builder()
                .sourceAccountId(null) // Giải ngân từ ngân hàng
                .destinationAccountId(account.getAccountId())
                .amount(loan.getDisbursedAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionType(TransactionType.DEPOSIT)
                .channel(TransactionChannel.INTERNAL)
                .status(TransactionStatus.COMPLETED)
                .description("Loan disbursement for loan: " + loanServiceRef)
                .traceId("LOAN-DISBURSE-" + loanServiceRef)
                .build();
        transaction = transactionRepository.save(transaction);

        // 6. Update loan status
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loanRepository.save(loan);

        log.info("[LOAN-DISBURSE-002] Loan disbursed successfully. TxId: {}, Amount: {}", 
                 transaction.getTransactionId(), loan.getDisbursedAmount());

        return LoanDisbursementResponse.builder()
                .loanId(loan.getLoanId())
                .loanServiceRef(loan.getLoanServiceRef())
                .transactionId(transaction.getTransactionId())
                .disbursedAmount(loan.getDisbursedAmount())
                .balanceAfter(balanceAfter)
                .status("SUCCESS")
                .message("Loan disbursed successfully")
                .disbursementTime(transaction.getTransactionDate())
                .build();
    }

    @Override
    @Transactional
    public LoanRepaymentResponse repayLoan(LoanRepaymentRequest request) {
        log.info("[LOAN-REPAY-001] Processing loan repayment. LoanRef: {}, Amount: {}", 
                 request.getLoanServiceRef(), request.getAmount());

        // 1. Tìm loan
        Loan loan = loanRepository.findByLoanServiceRef(request.getLoanServiceRef())
                .orElseThrow(() -> new BusinessException("Loan not found: " + request.getLoanServiceRef()));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new BusinessException("Loan is not active or overdue");
        }

        // 2. Kiểm tra account và số dư
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BusinessException("Account not found: " + request.getAccountId()));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient balance for loan repayment");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());

        // 3. Debit tiền từ tài khoản
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        // 4. Update dư nợ
        BigDecimal newOutstanding = loan.getOutstandingPrincipal().subtract(request.getPrincipalAmount());
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        loan.setOutstandingPrincipal(newOutstanding);
        loan.setTotalInterestPaid(loan.getTotalInterestPaid().add(request.getInterestAmount()));
        
        if (request.getPenaltyAmount() != null) {
            loan.setTotalPenaltyPaid(loan.getTotalPenaltyPaid().add(request.getPenaltyAmount()));
        }

        // Nếu trả hết → CLOSED
        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        } else if (loan.getStatus() == LoanStatus.OVERDUE) {
            loan.setStatus(LoanStatus.ACTIVE); // Reset về ACTIVE nếu đã trả
        }

        loanRepository.save(loan);

        // 5. Ghi ledger
        Transaction transaction = Transaction.builder()
                .sourceAccountId(account.getAccountId())
                .destinationAccountId(null) // Thu nợ về ngân hàng
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionType(TransactionType.WITHDRAWAL)
                .channel(TransactionChannel.INTERNAL)
                .status(TransactionStatus.COMPLETED)
                .description("Loan repayment for loan: " + request.getLoanServiceRef() + 
                           " (Schedule: " + request.getScheduleRef() + ")")
                .traceId("LOAN-REPAY-" + request.getLoanServiceRef() + "-" + request.getScheduleRef())
                .build();
        transaction = transactionRepository.save(transaction);

        log.info("[LOAN-REPAY-002] Repayment successful. TxId: {}, Outstanding: {}", 
                 transaction.getTransactionId(), newOutstanding);

        return LoanRepaymentResponse.builder()
                .loanId(loan.getLoanId())
                .loanServiceRef(loan.getLoanServiceRef())
                .transactionId(transaction.getTransactionId())
                .paidAmount(request.getAmount())
                .principalPaid(request.getPrincipalAmount())
                .interestPaid(request.getInterestAmount())
                .penaltyPaid(request.getPenaltyAmount())
                .outstandingPrincipal(newOutstanding)
                .balanceAfter(balanceAfter)
                .status("SUCCESS")
                .message("Loan repayment successful")
                .repaymentTime(transaction.getTransactionDate())
                .build();
    }

    @Override
    public BigDecimal calculateAccruedInterest(String loanServiceRef, LocalDate asOfDate) {
        log.info("[LOAN-INTEREST-001] Calculating accrued interest for loan: {}", loanServiceRef);

        Loan loan = loanRepository.findByLoanServiceRef(loanServiceRef)
                .orElseThrow(() -> new BusinessException("Loan not found: " + loanServiceRef));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            return BigDecimal.ZERO;
        }

        LocalDate startDate = loan.getDisbursementDate();
        LocalDate endDate = asOfDate != null ? asOfDate : LocalDate.now();

        if (endDate.isBefore(startDate)) {
            return BigDecimal.ZERO;
        }

        // Tính số ngày tích lũy lãi
        long daysAccrued = ChronoUnit.DAYS.between(startDate, endDate);

        // Lãi phát sinh = dư nợ × lãi suất × số ngày / 365
        BigDecimal accruedInterest = loan.getOutstandingPrincipal()
                .multiply(loan.getInterestRate())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(daysAccrued))
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);

        log.info("[LOAN-INTEREST-002] Accrued interest: {} (Days: {})", accruedInterest, daysAccrued);
        return accruedInterest;
    }

    @Override
    public LoanInfoResponse getLoanInfo(String loanServiceRef) {
        log.info("[LOAN-INFO-001] Getting loan info: {}", loanServiceRef);

        Loan loan = loanRepository.findByLoanServiceRef(loanServiceRef)
                .orElseThrow(() -> new BusinessException("Loan not found: " + loanServiceRef));

        return loanMapper.toInfoResponse(loan);
    }

    @Override
    @Transactional
    public LoanRepaymentResponse closeLoan(String loanServiceRef, String accountId) {
        log.info("[LOAN-CLOSE-001] Closing loan: {}", loanServiceRef);

        Loan loan = loanRepository.findByLoanServiceRef(loanServiceRef)
                .orElseThrow(() -> new BusinessException("Loan not found: " + loanServiceRef));

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BusinessException("Loan already closed");
        }

        // Tính tổng còn phải trả
        BigDecimal accruedInterest = calculateAccruedInterest(loanServiceRef, LocalDate.now());
        BigDecimal totalDue = loan.getOutstandingPrincipal().add(accruedInterest);

        // Thu toàn bộ
        LoanRepaymentRequest closeRequest = LoanRepaymentRequest.builder()
                .loanServiceRef(loanServiceRef)
                .accountId(accountId)
                .amount(totalDue)
                .principalAmount(loan.getOutstandingPrincipal())
                .interestAmount(accruedInterest)
                .penaltyAmount(BigDecimal.ZERO)
                .scheduleRef("EARLY_SETTLEMENT")
                .notes("Early loan settlement")
                .build();

        LoanRepaymentResponse response = repayLoan(closeRequest);

        // Đóng loan
        loan.setStatus(LoanStatus.CLOSED);
        loanRepository.save(loan);

        log.info("[LOAN-CLOSE-002] Loan closed successfully");
        response.setMessage("Loan closed successfully with early settlement");
        
        return response;
    }
}

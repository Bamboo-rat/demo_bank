package com.example.notificationserrvice.events.loan;

import com.example.notificationserrvice.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Loan Notification Service - Handle loan event notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationService {
    
    private final EmailNotificationService emailService;
    
    private static final NumberFormat VND_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public void handleLoanApproved(LoanApprovedEvent event) {
        log.info("[NOTIF-LOAN-APPROVED] Sending loan approval notification to: {}", event.getCustomerEmail());
        
        String subject = "Khoản vay của bạn đã được duyệt - KLB Bank";
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Chúng tôi xin thông báo khoản vay của quý khách đã được duyệt với các thông tin sau:
            
            - Số khoản vay: %s
            - Số tiền được duyệt: %s VND
            - Lãi suất: %s%%/năm
            - Thời hạn: %d tháng
            - Ngày giải ngân dự kiến: %s
            
            Khoản vay sẽ được giải ngân trong thời gian sớm nhất.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            event.getLoanAccountId(),
            VND_FORMAT.format(event.getApprovedAmount()),
            event.getInterestRate(),
            event.getTermMonths(),
            event.getExpectedDisbursementDate().format(DATE_FORMAT)
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
    
    public void handleLoanDisbursed(LoanDisbursedEvent event) {
        log.info("[NOTIF-LOAN-DISBURSED] Sending disbursement notification to: {}", event.getCustomerEmail());
        
        String subject = "Khoản vay đã được giải ngân - KLB Bank";
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Khoản vay của quý khách đã được giải ngân thành công:
            
            - Số khoản vay: %s
            - Số tiền giải ngân: %s VND
            - Tài khoản nhận: %s
            - Mã giao dịch: %s
            - Thời gian: %s
            
            Tiền đã được chuyển vào tài khoản của quý khách.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            event.getLoanAccountId(),
            VND_FORMAT.format(event.getDisbursedAmount()),
            event.getAccountId(),
            event.getTransactionId(),
            event.getDisbursementTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
    
    public void handleRepaymentDue(RepaymentDueEvent event) {
        log.info("[NOTIF-REPAYMENT-DUE] Sending payment reminder to: {}", event.getCustomerEmail());
        
        String subject = String.format("Nhắc nhở thanh toán khoản vay - Kỳ %d - KLB Bank", 
                event.getInstallmentNumber());
        
        String urgency = event.getDaysUntilDue() == 0 ? "HÔM NAY" : 
                        event.getDaysUntilDue() == 1 ? "NGÀY MAI" : 
                        "trong " + event.getDaysUntilDue() + " ngày nữa";
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Chúng tôi xin nhắc nhở quý khách về kỳ thanh toán sắp đến hạn:
            
            - Số khoản vay: %s
            - Kỳ thanh toán: %d
            - Ngày đến hạn: %s (%s)
            - Số tiền phải trả: %s VND
              + Gốc: %s VND
              + Lãi: %s VND
            
            Vui lòng đảm bảo tài khoản có đủ số dư để tránh phát sinh phí phạt.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            event.getLoanAccountId(),
            event.getInstallmentNumber(),
            event.getDueDate().format(DATE_FORMAT),
            urgency,
            VND_FORMAT.format(event.getDueAmount()),
            VND_FORMAT.format(event.getPrincipalAmount()),
            VND_FORMAT.format(event.getInterestAmount())
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
    
    public void handleRepaymentSuccess(RepaymentSuccessEvent event) {
        log.info("[NOTIF-REPAYMENT-SUCCESS] Sending payment confirmation to: {}", event.getCustomerEmail());
        
        String subject = String.format("Thanh toán khoản vay thành công - Kỳ %d - KLB Bank", 
                event.getInstallmentNumber());
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Chúng tôi xác nhận đã nhận được khoản thanh toán của quý khách:
            
            - Số khoản vay: %s
            - Kỳ thanh toán: %d
            - Số tiền đã trả: %s VND
              + Gốc: %s VND
              + Lãi: %s VND
            - Mã giao dịch: %s
            - Thời gian: %s
            
            Dư nợ còn lại: %s VND
            
            Cảm ơn quý khách đã thanh toán đúng hạn.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            event.getLoanAccountId(),
            event.getInstallmentNumber(),
            VND_FORMAT.format(event.getPaidAmount()),
            VND_FORMAT.format(event.getPrincipalPaid()),
            VND_FORMAT.format(event.getInterestPaid()),
            event.getTransactionId(),
            event.getPaymentTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            VND_FORMAT.format(event.getOutstandingPrincipal())
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
    
    public void handleLoanOverdue(LoanOverdueEvent event) {
        log.info("[NOTIF-LOAN-OVERDUE] Sending overdue notification to: {}", event.getCustomerEmail());
        
        String subject = String.format("CẢNH BÁO: Khoản vay quá hạn %d ngày - KLB Bank", 
                event.getDaysOverdue());
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Khoản vay của quý khách đã quá hạn thanh toán:
            
            - Số khoản vay: %s
            - Kỳ thanh toán: %d
            - Ngày đến hạn: %s
            - Số ngày quá hạn: %d ngày
            
            Số tiền cần thanh toán:
            - Gốc quá hạn: %s VND
            - Lãi quá hạn: %s VND
            - Phí phạt: %s VND
            - TỔNG CỘNG: %s VND
            
            Vui lòng thanh toán ngay để tránh ảnh hưởng đến lịch sử tín dụng.
            
            Liên hệ hotline: 1900-xxxx để được hỗ trợ.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            event.getLoanAccountId(),
            event.getInstallmentNumber(),
            event.getDueDate().format(DATE_FORMAT),
            event.getDaysOverdue(),
            VND_FORMAT.format(event.getPrincipalOverdue()),
            VND_FORMAT.format(event.getInterestOverdue()),
            VND_FORMAT.format(event.getPenaltyAmount()),
            VND_FORMAT.format(event.getTotalDue())
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
    
    public void handleLoanClosed(LoanClosedEvent event) {
        log.info("[NOTIF-LOAN-CLOSED] Sending loan closure notification to: {}", event.getCustomerEmail());
        
        String subject = "Khoản vay đã được tất toán - KLB Bank";
        
        String settlementType = event.isEarlySettlement() ? "tất toán trước hạn" : "hoàn thành đầy đủ";
        
        String body = String.format("""
            Kính gửi quý khách %s,
            
            Chúng tôi xin thông báo khoản vay của quý khách đã được %s:
            
            - Số khoản vay: %s
            - Tổng số tiền đã trả: %s VND
            - Tổng lãi đã trả: %s VND
            - Tổng phí phạt: %s VND
            - Mã giao dịch: %s
            - Thời gian đóng: %s
            
            Cảm ơn quý khách đã sử dụng dịch vụ của KLB Bank.
            
            Trân trọng,
            KLB Bank
            """,
            event.getCustomerName(),
            settlementType,
            event.getLoanAccountId(),
            VND_FORMAT.format(event.getTotalPaid()),
            VND_FORMAT.format(event.getTotalInterestPaid()),
            VND_FORMAT.format(event.getTotalPenaltyPaid()),
            event.getTransactionId(),
            event.getClosedTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        emailService.sendTextEmail(event.getCustomerEmail(), subject, body);
    }
}

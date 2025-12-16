package com.example.notificationserrvice.service.impl;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import com.example.notificationserrvice.config.SendGridProperties;
import com.example.notificationserrvice.service.EmailNotificationService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * SendGrid implementation của EmailNotificationService
 */
@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "sendgrid", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailServiceImpl implements EmailNotificationService {

    private final SendGrid sendGrid;
    private final SendGridProperties sendGridProperties;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    public void sendTransactionSuccessEmail(TransactionNotificationEvent event) {
        if (!emailEnabled) {
            log.info("Email notification is disabled");
            return;
        }

        try {
            // Gửi email cho người gửi
            if (event.getSenderEmail() != null && !event.getSenderEmail().isBlank()) {
                sendSenderNotification(event);
            }

            // Gửi email cho người nhận
            if (event.getReceiverEmail() != null && !event.getReceiverEmail().isBlank()) {
                sendReceiverNotification(event);
            }
        } catch (Exception e) {
            log.error("Failed to send transaction notification emails", e);
        }
    }

    /**
     * Gửi email cho người gửi tiền
     */
    private void sendSenderNotification(TransactionNotificationEvent event) {
        String subject = "Chuyển tiền thành công - KLB Bank";
        String content = buildSenderEmailContent(event);
        
        sendEmail(event.getSenderEmail(), event.getSenderName(), subject, content);
    }

    /**
     * Gửi email cho người nhận tiền
     */
    private void sendReceiverNotification(TransactionNotificationEvent event) {
        String subject = "Bạn vừa nhận được tiền - KLB Bank";
        String content = buildReceiverEmailContent(event);
        
        sendEmail(event.getReceiverEmail(), event.getReceiverName(), subject, content);
    }

    /**
     * Gửi email qua SendGrid
     */
    private void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            Email from = new Email(
                sendGridProperties.getFrom().getEmail(), 
                sendGridProperties.getFrom().getName()
            );
            Email to = new Email(toEmail, toName);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to {} - Status: {}", toEmail, response.getStatusCode());
            } else {
                log.error("Failed to send email to {} - Status: {}, Body: {}", 
                         toEmail, response.getStatusCode(), response.getBody());
            }
            
        } catch (IOException e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Tạo nội dung email cho người gửi tiền
     */
    private String buildSenderEmailContent(TransactionNotificationEvent event) {
        String formattedAmount = formatCurrency(event.getAmount());
        String formattedDateTime = event.getTransactionTime().format(DATE_TIME_FORMATTER);
        String formattedBalance = formatCurrency(event.getSenderBalanceAfter());

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .transaction-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #666; }
                    .detail-value { color: #333; }
                    .amount { font-size: 24px; color: #e74c3c; font-weight: bold; }
                    .balance { font-size: 20px; color: #27ae60; font-weight: bold; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                    .status-badge { background: #27ae60; color: white; padding: 5px 15px; border-radius: 20px; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>KLB Bank</h1>
                        <p style="margin: 0;">Thông báo giao dịch</p>
                    </div>
                    <div class="content">
                        <h2 style="color: #667eea;">Chuyển tiền thành công</h2>
                        <p>Kính gửi <strong>%s</strong>,</p>
                        <p>Giao dịch chuyển tiền của bạn đã được thực hiện thành công.</p>
                        
                        <div class="transaction-details">
                            <div class="detail-row">
                                <span class="detail-label">Mã giao dịch:</span>
                                <span class="detail-value">#%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Thời gian:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số tiền chuyển:</span>
                                <span class="amount">- %s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Người nhận:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số tài khoản nhận:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Nội dung:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row" style="border-bottom: none; margin-top: 15px; padding-top: 15px; border-top: 2px solid #667eea;">
                                <span class="detail-label">Số dư mới:</span>
                                <span class="balance">%s</span>
                            </div>
                        </div>
                        
                        <p style="color: #888; font-size: 14px; margin-top: 20px;">
                        Nếu bạn không thực hiện giao dịch này, vui lòng liên hệ ngay với chúng tôi qua hotline: <strong>1900-xxxx</strong>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 KLB Bank. All rights reserved.</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            event.getSenderName(),
            event.getTransactionId(),
            formattedDateTime,
            formattedAmount,
            event.getReceiverName(),
            event.getReceiverAccountNumber(),
            event.getDescription() != null ? event.getDescription() : "Không có ghi chú",
            formattedBalance
        );
    }

    /**
     * Tạo nội dung email cho người nhận tiền
     */
    private String buildReceiverEmailContent(TransactionNotificationEvent event) {
        String formattedAmount = formatCurrency(event.getAmount());
        String formattedDateTime = event.getTransactionTime().format(DATE_TIME_FORMATTER);
        String formattedBalance = formatCurrency(event.getReceiverBalanceAfter());

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .transaction-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #666; }
                    .detail-value { color: #333; }
                    .amount { font-size: 24px; color: #27ae60; font-weight: bold; }
                    .balance { font-size: 20px; color: #27ae60; font-weight: bold; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>KLB Bank</h1>
                        <p style="margin: 0;">Thông báo giao dịch</p>
                    </div>
                    <div class="content">
                        <h2 style="color: #27ae60;">Bạn vừa nhận được tiền</h2>
                        <p>Kính gửi <strong>%s</strong>,</p>
                        <p>Tài khoản của bạn vừa nhận được một khoản tiền.</p>
                        
                        <div class="transaction-details">
                            <div class="detail-row">
                                <span class="detail-label">Mã giao dịch:</span>
                                <span class="detail-value">#%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Thời gian:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số tiền nhận:</span>
                                <span class="amount">+ %s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Người gửi:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số tài khoản gửi:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Nội dung:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row" style="border-bottom: none; margin-top: 15px; padding-top: 15px; border-top: 2px solid #27ae60;">
                                <span class="detail-label">Số dư mới:</span>
                                <span class="balance">%s</span>
                            </div>
                        </div>
                        
                        <p style="color: #888; font-size: 14px; margin-top: 20px;">
                        Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi qua hotline: <strong>1900-xxxx</strong>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 KLB Bank. All rights reserved.</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            event.getReceiverName(),
            event.getTransactionId(),
            formattedDateTime,
            formattedAmount,
            event.getSenderName(),
            event.getSenderAccountNumber(),
            event.getDescription() != null ? event.getDescription() : "Không có ghi chú",
            formattedBalance
        );
    }

    /**
     * Format số tiền theo định dạng VND
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return CURRENCY_FORMATTER.format(amount);
    }

    @Override
    public void sendSimpleEmail(String toEmail, String toName, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.info("Email notification is disabled");
            return;
        }
        
        sendEmail(toEmail, toName, subject, htmlContent);
    }
}

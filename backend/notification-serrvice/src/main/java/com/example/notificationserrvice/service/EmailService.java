package com.example.notificationserrvice.service;

import com.example.commonapi.dto.notification.TransactionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service gửi email notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@klbbank.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Gửi email thông báo khi chuyển tiền thành công
     */
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
            log.error("Failed to send transaction notification email for transaction: {}", 
                    event.getTransactionReference(), e);
        }
    }

    /**
     * Gửi email cho người gửi tiền
     */
    private void sendSenderNotification(TransactionNotificationEvent event) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(event.getSenderEmail());
        helper.setSubject("Giao dịch chuyển tiền thành công - " + event.getTransactionReference());

        String htmlContent = buildSenderEmailContent(event);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Sender notification email sent to: {} for transaction: {}", 
                event.getSenderEmail(), event.getTransactionReference());
    }

    /**
     * Gửi email cho người nhận tiền
     */
    private void sendReceiverNotification(TransactionNotificationEvent event) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(event.getReceiverEmail());
        helper.setSubject("Bạn nhận được khoản chuyển tiền - " + event.getTransactionReference());

        String htmlContent = buildReceiverEmailContent(event);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Receiver notification email sent to: {} for transaction: {}", 
                event.getReceiverEmail(), event.getTransactionReference());
    }

    /**
     * Build HTML content cho email người gửi
     */
    private String buildSenderEmailContent(TransactionNotificationEvent event) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; }
                        .info-row { margin: 10px 0; }
                        .label { font-weight: bold; display: inline-block; width: 180px; }
                        .value { display: inline-block; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                        .success { color: #4CAF50; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Giao dịch chuyển tiền thành công</h2>
                        </div>
                        <div class="content">
                            <p>Kính gửi <strong>%s</strong>,</p>
                            <p>Giao dịch chuyển tiền của bạn đã được thực hiện thành công.</p>
                            
                            <h3>Thông tin giao dịch:</h3>
                            <div class="info-row">
                                <span class="label">Mã giao dịch:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Số tiền:</span>
                                <span class="value success">%s %s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Tài khoản nguồn:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Tài khoản đích:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Người nhận:</span>
                                <span class="value">%s</span>
                            </div>
                            %s
                            <div class="info-row">
                                <span class="label">Nội dung:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Thời gian:</span>
                                <span class="value">%s</span>
                            </div>
                            %s
                        </div>
                        <div class="footer">
                            <p>Đây là email tự động, vui lòng không trả lời email này.</p>
                            <p>© 2024 KLB Bank. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                event.getSenderName(),
                event.getTransactionReference(),
                currencyFormat.format(event.getAmount()),
                event.getCurrency(),
                event.getSenderAccountNumber(),
                event.getReceiverAccountNumber(),
                event.getReceiverName(),
                event.isInternalTransfer() ? "" : String.format(
                        "<div class=\"info-row\"><span class=\"label\">Ngân hàng thụ hưởng:</span><span class=\"value\">%s</span></div>",
                        event.getReceiverBankName()),
                event.getDescription() != null ? event.getDescription() : "",
                event.getTransactionTime().format(dateFormat),
                event.getSenderBalanceAfter() != null ? String.format(
                        "<div class=\"info-row\"><span class=\"label\">Số dư khả dụng:</span><span class=\"value\">%s %s</span></div>",
                        currencyFormat.format(event.getSenderBalanceAfter()),
                        event.getCurrency()) : ""
        );
    }

    /**
     * Build HTML content cho email người nhận
     */
    private String buildReceiverEmailContent(TransactionNotificationEvent event) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; }
                        .info-row { margin: 10px 0; }
                        .label { font-weight: bold; display: inline-block; width: 180px; }
                        .value { display: inline-block; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                        .success { color: #4CAF50; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Bạn nhận được khoản chuyển tiền</h2>
                        </div>
                        <div class="content">
                            <p>Kính gửi <strong>%s</strong>,</p>
                            <p>Tài khoản của bạn vừa nhận được một khoản chuyển tiền.</p>
                            
                            <h3>Thông tin giao dịch:</h3>
                            <div class="info-row">
                                <span class="label">Mã giao dịch:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Số tiền nhận:</span>
                                <span class="value success">+ %s %s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Từ tài khoản:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Người gửi:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Nội dung:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Thời gian:</span>
                                <span class="value">%s</span>
                            </div>
                            %s
                        </div>
                        <div class="footer">
                            <p>Đây là email tự động, vui lòng không trả lời email này.</p>
                            <p>© 2024 KLB Bank. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                event.getReceiverName(),
                event.getTransactionReference(),
                currencyFormat.format(event.getAmount()),
                event.getCurrency(),
                event.getSenderAccountNumber(),
                event.getSenderName(),
                event.getDescription() != null ? event.getDescription() : "",
                event.getTransactionTime().format(dateFormat),
                event.getReceiverBalanceAfter() != null ? String.format(
                        "<div class=\"info-row\"><span class=\"label\">Số dư mới:</span><span class=\"value\">%s %s</span></div>",
                        currencyFormat.format(event.getReceiverBalanceAfter()),
                        event.getCurrency()) : ""
        );
    }
}

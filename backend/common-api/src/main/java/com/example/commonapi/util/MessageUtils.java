package com.example.commonapi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    /**
     * Get message by code using current locale from context
     * Lấy thông báo theo mã sử dụng ngôn ngữ hiện tại từ context
     *
     * @param code Message code
     * @return Localized message
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by code with arguments using current locale
     * Lấy thông báo theo mã với tham số sử dụng ngôn ngữ hiện tại
     *
     * @param code Message code
     * @param args Message arguments
     * @return Localized message with arguments replaced
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by code with default message if not found
     * Lấy thông báo theo mã với thông báo mặc định nếu không tìm thấy
     *
     * @param code Message code
     * @param defaultMessage Default message
     * @return Localized message or default message
     */
    public String getMessageOrDefault(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by code with specific locale
     * Lấy thông báo theo mã với ngôn ngữ cụ thể
     *
     * @param code Message code
     * @param locale Specific locale
     * @return Localized message
     */
    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    /**
     * Get message by code with arguments and specific locale
     * Lấy thông báo theo mã với tham số và ngôn ngữ cụ thể
     *
     * @param code Message code
     * @param args Message arguments
     * @param locale Specific locale
     * @return Localized message
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * Get Vietnamese message (vi-VN)
     * Lấy thông báo tiếng Việt
     *
     * @param code Message code
     * @return Vietnamese message
     */
    public String getVietnameseMessage(String code) {
        return messageSource.getMessage(code, null, new Locale("vi", "VN"));
    }

    /**
     * Get Vietnamese message with arguments
     * Lấy thông báo tiếng Việt với tham số
     *
     * @param code Message code
     * @param args Message arguments
     * @return Vietnamese message with arguments
     */
    public String getVietnameseMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, new Locale("vi", "VN"));
    }

    /**
     * Get English message (en-US) - for future use
     * Lấy thông báo tiếng Anh - để sử dụng sau này
     *
     * @param code Message code
     * @return English message
     */
    public String getEnglishMessage(String code) {
        return messageSource.getMessage(code, null, Locale.US);
    }

    /**
     * Get English message with arguments - for future use
     * Lấy thông báo tiếng Anh với tham số - để sử dụng sau này
     *
     * @param code Message code
     * @param args Message arguments
     * @return English message with arguments
     */
    public String getEnglishMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.US);
    }
}

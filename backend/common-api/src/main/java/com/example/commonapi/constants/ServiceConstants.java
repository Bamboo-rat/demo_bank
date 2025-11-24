package com.example.commonapi.constants;

/**
 * Common constants used across microservices
 */
public final class ServiceConstants {

    // Service Names
    public static final String CUSTOMER_SERVICE = "customer-service";
    public static final String ACCOUNT_SERVICE = "account-service";
    public static final String CORE_BANKING_SERVICE = "core-banking-service";

    // Dubbo Service Versions
    public static final String DUBBO_VERSION = "1.0.0";

    // Dubbo Timeouts (milliseconds)
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int LONG_OPERATION_TIMEOUT = 30000;

    // Request Sources
    public static final String REQUEST_SOURCE_WEB = "WEB";
    public static final String REQUEST_SOURCE_MOBILE = "MOBILE";
    public static final String REQUEST_SOURCE_API = "API";
    public static final String REQUEST_SOURCE_INTERNAL = "INTERNAL";

    // Common Headers
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_SERVICE_NAME = "X-Service-Name";

    // Default Values
    public static final String DEFAULT_CURRENCY = "VND";
    public static final String DEFAULT_LOCALE = "vi_VN";

    private ServiceConstants() {
        // Prevent instantiation
    }
}
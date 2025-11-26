package com.example.customerservice.redis.model;

public enum RegistrationSessionStatus {
    OTP_PENDING,
    OTP_VERIFIED,
    PROFILE_IN_PROGRESS,
    READY_FOR_CREATION,
    COMPLETED
}

package com.example.transactionservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeePaymentMethod {
    SOURCE("fee.payment.source", "Fee paid by source account"),
    DESTINATION("fee.payment.destination", "Fee paid by destination account");

    private final String messageCode;
    private final String description;
}

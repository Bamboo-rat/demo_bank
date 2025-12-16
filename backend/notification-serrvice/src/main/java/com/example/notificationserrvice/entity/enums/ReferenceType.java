package com.example.notificationserrvice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReferenceType {
    
    TRANSACTION("notification.reference.transaction.name", "notification.reference.transaction.description"),
    
    ACCOUNT("notification.reference.account.name", "notification.reference.account.description"),
 
    LOAN("notification.reference.loan.name", "notification.reference.loan.description");

    private final String nameMessageCode;
    private final String descriptionMessageCode;
}

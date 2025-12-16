package com.example.notificationserrvice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    BALANCE_CHANGE("notification.type.balance_change.name", "notification.type.balance_change.description"),
 
    SYSTEM("notification.type.system.name", "notification.type.system.description"),
  
    SECURITY("notification.type.security.name", "notification.type.security.description"),
   
    LOAN("notification.type.loan.name", "notification.type.loan.description");

    private final String nameMessageCode;
    private final String descriptionMessageCode;
}

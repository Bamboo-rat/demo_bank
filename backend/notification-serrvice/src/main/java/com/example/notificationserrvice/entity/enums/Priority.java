package com.example.notificationserrvice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum Priority {
   
    LOW("notification.priority.low.name", "notification.priority.low.description", 1),
    
    NORMAL("notification.priority.normal.name", "notification.priority.normal.description", 2),
    
    HIGH("notification.priority.high.name", "notification.priority.high.description", 3);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final int level;

    /**
     * Kiểm tra xem priority này có cao hơn priority khác không
     */
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}

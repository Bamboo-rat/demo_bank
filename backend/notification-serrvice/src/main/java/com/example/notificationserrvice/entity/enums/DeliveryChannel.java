package com.example.notificationserrvice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum DeliveryChannel {
    IN_APP("notification.channel.in_app.name", "notification.channel.in_app.description", true),
  
    EMAIL("notification.channel.email.name", "notification.channel.email.description", false),
   
    SMS("notification.channel.sms.name", "notification.channel.sms.description", false);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isRealTime;

    /**
     * Kiểm tra xem kênh này có gửi thông báo real-time không
     */
    public boolean isRealtimeChannel() {
        return isRealTime;
    }
}

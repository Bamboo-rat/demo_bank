package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionChannel {
    ATM("transaction.channel.atm.name", "transaction.channel.atm.description", false),
    MOBILE("transaction.channel.mobile.name", "transaction.channel.mobile.description", true),
    INTERNET_BANKING("transaction.channel.internet.name", "transaction.channel.internet.description", true),
    TELLER("transaction.channel.teller.name", "transaction.channel.teller.description", false),
    INTERNAL("transaction.channel.internal.name", "transaction.channel.internal.description", true),
    POS("transaction.channel.pos.name", "transaction.channel.pos.description", false),
    API("transaction.channel.api.name", "transaction.channel.api.description", true);

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final boolean isAutomated;

    /**
     * Kiểm tra xem kênh này có yêu cầu xác thực teller không
     * @return true nếu cần teller approval
     */
    public boolean requiresTellerApproval() {
        return this == TELLER;
    }

    /**
     * Kiểm tra xem kênh này có tự động không
     * @return true nếu là kênh tự động
     */
    public boolean isAutomatedChannel() {
        return isAutomated;
    }
}

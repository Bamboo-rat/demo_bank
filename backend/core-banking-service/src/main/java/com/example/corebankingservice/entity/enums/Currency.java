package com.example.corebankingservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    VND(
        "currency.vnd.name",
        "currency.vnd.symbol",
        "₫",
        0  // No decimal places
    ),
    USD(
        "currency.usd.name",
        "currency.usd.symbol",
        "$",
        2  // 2 decimal places
    ),
    EUR(
        "currency.eur.name",
        "currency.eur.symbol",
        "€",
        2
    ),
    JPY(
        "currency.jpy.name",
        "currency.jpy.symbol",
        "¥",
        0
    );

    private final String nameMessageCode;
    private final String symbolMessageCode;
    private final String symbol;
    private final int decimalPlaces;

    /**
     * Check if currency requires decimal places
     * @return true if decimal places are used
     */
    public boolean hasDecimals() {
        return decimalPlaces > 0;
    }

    /**
     * Check if this is the default currency
     * @return true if VND (default for Vietnam)
     */
    public boolean isDefaultCurrency() {
        return this == VND;
    }
}


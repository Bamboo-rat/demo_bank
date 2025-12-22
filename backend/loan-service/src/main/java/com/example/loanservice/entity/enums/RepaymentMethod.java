package com.example.loanservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Phương thức trả nợ
 */
@Getter
@RequiredArgsConstructor
public enum RepaymentMethod {
    /**
     * Gốc đều - lãi giảm dần (chuẩn ngân hàng)
     */
    EQUAL_PRINCIPAL("repayment.method.equal_principal.name", "repayment.method.equal_principal.description", "Trả gốc đều hàng tháng, lãi tính trên số dư gốc giảm dần"),
    
    /**
     * Gốc + lãi đều (Annuity)
     */
    EQUAL_INSTALLMENT("repayment.method.equal_installment.name", "repayment.method.equal_installment.description", "Trả số tiền cố định mỗi tháng (gốc + lãi)"),
    
    /**
     * Trả lãi định kỳ - gốc cuối kỳ
     */
    INTEREST_ONLY_PRINCIPAL_END("repayment.method.interest_only_principal_end.name", "repayment.method.interest_only_principal_end.description", "Chỉ trả lãi hàng tháng, trả toàn bộ gốc vào cuối kỳ");

    private final String nameMessageCode;
    private final String descriptionMessageCode;
    private final String formula;

    /**
     * Check if method is commonly used
     * @return true if method is standard
     */
    public boolean isStandard() {
        return this == EQUAL_PRINCIPAL || this == EQUAL_INSTALLMENT;
    }

    /**
     * Check if method requires full principal at end
     * @return true if balloon payment required
     */
    public boolean requiresBalloonPayment() {
        return this == INTEREST_ONLY_PRINCIPAL_END;
    }
}

package com.example.accountservice.service;

import java.math.BigDecimal;

/**
 * Service để lấy lãi suất tiết kiệm
 * Snapshot lãi suất tại thời điểm mở sổ
 */
public interface SavingsInterestRateService {

    /**
     * Lấy lãi suất theo kỳ hạn và phương thức trả lãi
     * 
     * @param termMonths Số tháng kỳ hạn
     * @param paymentMethod Phương thức trả lãi (END_OF_TERM, MONTHLY, QUARTERLY, BEGINNING)
     * @return Lãi suất % (ví dụ: 5.5 nghĩa là 5.5%/năm)
     */
    BigDecimal getInterestRate(int termMonths, String paymentMethod);
}

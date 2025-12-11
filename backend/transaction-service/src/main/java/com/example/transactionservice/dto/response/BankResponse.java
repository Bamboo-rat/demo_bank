package com.example.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bank information response from VietQR API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankResponse {

    private Integer id;
    private String name;          // Tên đầy đủ ngân hàng
    private String code;          // Mã ngân hàng (VD: ACB, VCB)
    private String bin;           // Bank Identification Number
    private String shortName;     // Tên viết tắt
    private String logo;          // URL logo ngân hàng
    private Integer transferSupported;  // Hỗ trợ chuyển khoản
    private Integer lookupSupported;    // Hỗ trợ tra cứu
}

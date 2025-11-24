package com.example.customerservice.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String street; // Số nhà, tên đường
    private String ward;   // Phường/Xã
    private String district; // Quận/Huyện
    private String city;     // Tỉnh/Thành phố
    private String country;  // Quốc gia
}
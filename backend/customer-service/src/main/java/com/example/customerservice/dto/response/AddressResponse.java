package com.example.customerservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;
}
package com.example.customerservice.dto.request.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationVerifyRequest {

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "OTP cannot be blank")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
    private String otp;
}

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
public class RegistrationCompleteRequest {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;
}

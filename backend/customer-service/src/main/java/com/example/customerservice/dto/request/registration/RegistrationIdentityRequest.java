package com.example.customerservice.dto.request.registration;

import com.example.customerservice.dto.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationIdentityRequest {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[\\s\\./0-9]*$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "National ID không được để trống")
    private String nationalId;

    @NotNull(message = "Issue date of national ID không được null")
    @PastOrPresent(message = "Issue date must be in the past or present")
    private LocalDate issueDateNationalId;

    @NotBlank(message = "Place of issue for national ID không được để trống")
    private String placeOfIssueNationalId;

    @Valid
    @NotNull(message = "Permanent address cannot be null")
    private AddressRequest permanentAddress;

    @Valid
    private AddressRequest temporaryAddress;

    private String documentFrontImage;
    private String documentBackImage;
    private String selfieImage;
}

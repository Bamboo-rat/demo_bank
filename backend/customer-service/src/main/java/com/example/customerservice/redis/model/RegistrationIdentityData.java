package com.example.customerservice.redis.model;

import com.example.customerservice.dto.request.AddressRequest;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationIdentityData implements Serializable {

    private static final long serialVersionUID = -4183265707372766924L;

    private String nationalId;
    private LocalDate issueDateNationalId;
    private String placeOfIssueNationalId;
    private AddressRequest permanentAddress;
    private AddressRequest temporaryAddress;
    private String documentFrontImage;
    private String documentBackImage;
    private String selfieImage;
}

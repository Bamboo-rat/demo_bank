package com.example.corebankingservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCifStatusResponse {
    private String cifNumber;
    private String previousStatus;
    private String currentStatus;
    private String message;
    private Boolean success;
}

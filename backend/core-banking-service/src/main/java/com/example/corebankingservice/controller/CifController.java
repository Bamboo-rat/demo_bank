package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.request.CreateCifRequest;
import com.example.corebankingservice.dto.request.UpdateCifStatusRequest;
import com.example.corebankingservice.dto.response.CifResponse;
import com.example.corebankingservice.dto.response.CifStatusResponse;
import com.example.corebankingservice.service.impl.CifServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cif")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CIF Management", description = "Core Banking CIF Management APIs")
public class CifController {

    private final CifServiceImpl cifService;

    @PostMapping("/create")
    @Operation(summary = "Create new CIF", description = "Create a new CIF in Core Banking System")
    public ResponseEntity<ApiResponse<CifResponse>> createCif(
            @Valid @RequestBody CreateCifRequest request) {

        log.info("Received request to create CIF for username: {}", request.getUsername());

        CifResponse response = cifService.createCif(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CifResponse>builder()
                        .success(true)
                        .message("CIF created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/status/{cifNumber}")
    @Operation(summary = "Check CIF Status", description = "Get current status of a CIF")
    public ResponseEntity<ApiResponse<CifStatusResponse>> checkCifStatus(
            @Parameter(description = "CIF Number", required = true)
            @PathVariable String cifNumber) {

        log.info("Checking status for CIF: {}", cifNumber);

        CifStatusResponse response = cifService.getCifStatus(cifNumber);

        return ResponseEntity.ok(ApiResponse.<CifStatusResponse>builder()
                .success(true)
                .message("CIF status retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{cifNumber}/status")
    @Operation(summary = "Update CIF Status", description = "Update the status of a CIF")
    public ResponseEntity<ApiResponse<CifResponse>> updateCifStatus(
            @Parameter(description = "CIF Number", required = true)
            @PathVariable String cifNumber,
            @Valid @RequestBody UpdateCifStatusRequest request) {

        log.info("Updating status for CIF: {} with action: {}", cifNumber, request.getAction());

        CifResponse response = cifService.updateCifStatus(cifNumber, request);

        return ResponseEntity.ok(ApiResponse.<CifResponse>builder()
                .success(true)
                .message("CIF status updated successfully")
                .data(response)
                .build());
    }

    @GetMapping("/validate/{cifNumber}")
    @Operation(summary = "Validate CIF", description = "Check if CIF exists and can transact")
    public ResponseEntity<ApiResponse<Boolean>> validateCif(
            @Parameter(description = "CIF Number", required = true)
            @PathVariable String cifNumber) {

        log.info("Validating CIF: {}", cifNumber);

        try {
            CifStatusResponse status = cifService.getCifStatus(cifNumber);
            boolean canTransact = Boolean.TRUE.equals(status.getCanTransact());

            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .success(true)
                    .message(canTransact ? "CIF is valid and can transact" : "CIF cannot transact")
                    .data(canTransact)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("CIF validation failed: " + e.getMessage())
                    .data(false)
                    .build());
        }
    }
}
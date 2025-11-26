package com.example.customerservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.registration.RegistrationCompleteRequest;
import com.example.customerservice.dto.request.registration.RegistrationIdentityRequest;
import com.example.customerservice.dto.request.registration.RegistrationProfileRequest;
import com.example.customerservice.dto.request.registration.RegistrationStartRequest;
import com.example.customerservice.dto.request.registration.RegistrationVerifyRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.dto.response.registration.RegistrationSessionResponse;
import com.example.customerservice.dto.response.registration.RegistrationStartResponse;
import com.example.customerservice.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RegistrationStartResponse>> start(@Valid @RequestBody RegistrationStartRequest request) {
        RegistrationStartResponse response = registrationService.start(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("OTP đã được gửi", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> verify(@Valid @RequestBody RegistrationVerifyRequest request) {
        RegistrationSessionResponse response = registrationService.verify(request);
        return ResponseEntity.ok(ApiResponse.success("OTP xác thực thành công", response));
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> saveProfile(@Valid @RequestBody RegistrationProfileRequest request) {
        RegistrationSessionResponse response = registrationService.saveProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu thông tin cá nhân", response));
    }

    @PostMapping("/identity")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> saveIdentity(@Valid @RequestBody RegistrationIdentityRequest request) {
        RegistrationSessionResponse response = registrationService.saveIdentity(request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu thông tin định danh", response));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<CustomerResponse>> complete(@Valid @RequestBody RegistrationCompleteRequest request) {
        CustomerResponse response = registrationService.complete(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khách hàng thành công!", response));
    }
}

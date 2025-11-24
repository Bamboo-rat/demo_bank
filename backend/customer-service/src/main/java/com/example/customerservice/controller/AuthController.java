package com.example.customerservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(@Valid @RequestBody CustomerRegisterRequest registerDto) {
        CustomerResponse customerResponse = customerService.registerCustomer(registerDto);
        ApiResponse<CustomerResponse> response = ApiResponse.success("Đăng ký khách hàng thành công!", customerResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody CustomerLoginDTO loginDto) {
        Object tokenResponse = customerService.loginCustomer(loginDto);
        ApiResponse<Object> response = ApiResponse.success("Đăng nhập thành công!", tokenResponse);
        return ResponseEntity.ok(response);
    }

}
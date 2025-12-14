package com.example.customerservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.dto.response.CustomerResponse;
import com.example.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs xác thực và đăng ký")
public class AuthController {

    private final CustomerService customerService;

    @Operation(
            summary = "Đăng ký khách hàng mới",
            description = "API đăng ký tài khoản khách hàng mới. Tạo user trong Keycloak và lưu thông tin khách hàng."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc username/email đã tồn tại"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng ký khách hàng",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRegisterRequest.class))
            )
            @Valid @RequestBody CustomerRegisterRequest registerDto) {
        CustomerResponse customerResponse = customerService.registerCustomer(registerDto);
        ApiResponse<CustomerResponse> response = ApiResponse.success("Đăng ký khách hàng thành công!", customerResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Đăng nhập",
            description = "API đăng nhập hệ thống. Trả về access_token và refresh_token từ Keycloak."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Sai tên đăng nhập hoặc mật khẩu"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng nhập",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerLoginDTO.class))
            )
            @Valid @RequestBody CustomerLoginDTO loginDto) {
        Object tokenResponse = customerService.loginCustomer(loginDto);
        ApiResponse<Object> response = ApiResponse.success("Đăng nhập thành công!", tokenResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Làm mới token",
            description = "API làm mới access_token sử dụng refresh_token. Trả về cặp token mới."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Làm mới token thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Refresh token không hợp lệ hoặc thiếu"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token hết hạn hoặc không hợp lệ"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Refresh token is required"));
        }
        
        Object tokenResponse = customerService.refreshToken(refreshToken);
        ApiResponse<Object> response = ApiResponse.success("Token refreshed successfully!", tokenResponse);
        return ResponseEntity.ok(response);
    }

}
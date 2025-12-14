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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Registration Flow", description = "APIs flow đăng ký khách hàng theo từng bước (Multi-step registration)")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(
            summary = "Bước 1: Bắt đầu đăng ký",
            description = "API khởi tạo session đăng ký và gửi mã OTP qua email/SMS"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OTP đã được gửi",
                    content = @Content(schema = @Schema(implementation = RegistrationStartResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Thông tin không hợp lệ hoặc email/số điện thoại đã tồn tại"
            )
    })
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RegistrationStartResponse>> start(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin bắt đầu đăng ký (email/phone)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationStartRequest.class))
            )
            @Valid @RequestBody RegistrationStartRequest request) {
        RegistrationStartResponse response = registrationService.start(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("OTP đã được gửi", response));
    }

    @Operation(
            summary = "Bước 2: Xác thực OTP",
            description = "API xác thực mã OTP đã gửi. Nếu thành công, session được kích hoạt."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xác thực OTP thành công",
                    content = @Content(schema = @Schema(implementation = RegistrationSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "OTP không hợp lệ hoặc đã hết hạn"
            )
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> verify(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin xác thực OTP (sessionId và OTP code)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationVerifyRequest.class))
            )
            @Valid @RequestBody RegistrationVerifyRequest request) {
        RegistrationSessionResponse response = registrationService.verify(request);
        return ResponseEntity.ok(ApiResponse.success("OTP xác thực thành công", response));
    }

    @Operation(
            summary = "Bước 3: Lưu thông tin cá nhân",
            description = "API lưu thông tin cá nhân của khách hàng (họ tên, ngày sinh, địa chỉ, ...)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lưu thông tin thành công",
                    content = @Content(schema = @Schema(implementation = RegistrationSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc session không hợp lệ"
            )
    })
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> saveProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cá nhân khách hàng",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationProfileRequest.class))
            )
            @Valid @RequestBody RegistrationProfileRequest request) {
        RegistrationSessionResponse response = registrationService.saveProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu thông tin cá nhân", response));
    }

    @Operation(
            summary = "Bước 4: Lưu thông tin định danh",
            description = "API lưu thông tin định danh của khách hàng (CMND/CCCD, passport, ...)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lưu thông tin định danh thành công",
                    content = @Content(schema = @Schema(implementation = RegistrationSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc session không hợp lệ"
            )
    })
    @PostMapping("/identity")
    public ResponseEntity<ApiResponse<RegistrationSessionResponse>> saveIdentity(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin định danh khách hàng",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationIdentityRequest.class))
            )
            @Valid @RequestBody RegistrationIdentityRequest request) {
        RegistrationSessionResponse response = registrationService.saveIdentity(request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu thông tin định danh", response));
    }

    @Operation(
            summary = "Bước 5: Hoàn tất đăng ký",
            description = "API hoàn tất quá trình đăng ký. Tạo user trong Keycloak và lưu khách hàng vào database."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Đăng ký khách hàng thành công",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ, session không hợp lệ hoặc chưa điền đủ thông tin các bước trước"
            )
    })
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<CustomerResponse>> complete(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin hoàn tất đăng ký (username, password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistrationCompleteRequest.class))
            )
            @Valid @RequestBody RegistrationCompleteRequest request) {
        CustomerResponse response = registrationService.complete(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khách hàng thành công!", response));
    }
}

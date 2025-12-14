package com.example.corebankingservice.controller;

import com.example.commonapi.dto.ApiResponse;
import com.example.corebankingservice.dto.request.BalanceOperationRequest;
import com.example.corebankingservice.dto.response.BalanceOperationResponse;
import com.example.corebankingservice.service.BalanceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Controller for balance management operations
 * Source of Truth for all balance changes
 */
@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@Tag(name = "Balance Management", description = "APIs quản lý số dư tài khoản - Source of Truth cho mọi thay đổi số dư")
public class BalanceManagementController {

    private final BalanceManagementService balanceManagementService;
    private final MessageSource messageSource;

    @Operation(
            summary = "Trừ tiền từ tài khoản (Debit)",
            description = "API trừ tiền từ tài khoản. Được sử dụng bởi transaction service cho rút tiền và chuyển khoản. " +
                    "**Idempotent**: Nếu transactionReference đã tồn tại, sẽ trả về kết quả cũ thay vì xử lý lại."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trừ tiền thành công",
                    content = @Content(schema = @Schema(implementation = BalanceOperationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc số dư không đủ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy tài khoản"
            )
    })
    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> debit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin yêu cầu trừ tiền",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BalanceOperationRequest.class))
            )
            @RequestBody @Valid BalanceOperationRequest request) {
        BalanceOperationResponse response = balanceManagementService.debit(request);
        return ResponseEntity.ok(ApiResponse.success(getMessage("success.debit.api"), response));
    }

    @Operation(
            summary = "Cộng tiền vào tài khoản (Credit)",
            description = "API cộng tiền vào tài khoản. Được sử dụng bởi transaction service cho nạp tiền và nhận chuyển khoản. " +
                    "**Idempotent**: Nếu transactionReference đã tồn tại, sẽ trả về kết quả cũ thay vì xử lý lại."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cộng tiền thành công",
                    content = @Content(schema = @Schema(implementation = BalanceOperationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy tài khoản"
            )
    })
    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> credit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin yêu cầu cộng tiền",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BalanceOperationRequest.class))
            )
            @RequestBody @Valid BalanceOperationRequest request) {
        BalanceOperationResponse response = balanceManagementService.credit(request);
        return ResponseEntity.ok(ApiResponse.success(getMessage("success.credit.api"), response));
    }

    @Operation(
            summary = "Giữ tiền tạm thời (Hold)",
            description = "API giữ tiền tạm thời cho giao dịch chờ xử lý. Số tiền sẽ bị khóa không cho chi tiêu nhưng vẫn còn trong tài khoản."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giữ tiền thành công",
                    content = @Content(schema = @Schema(implementation = BalanceOperationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Số dư khả dụng không đủ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy tài khoản"
            )
    })
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> holdAmount(
            @Parameter(description = "Số tài khoản cần giữ tiền", required = true, example = "37029832305730")
            @RequestParam String accountNumber,
            @Parameter(description = "Số tiền cần giữ (VND)", required = true, example = "100000.00")
            @RequestParam BigDecimal amount,
            @Parameter(description = "Mã tham chiếu giao dịch (UUID)", required = true, example = "TX-20241214-001")
            @RequestParam String transactionReference) {
        BalanceOperationResponse response = balanceManagementService.holdAmount(
                accountNumber, amount, transactionReference);
        return ResponseEntity.ok(ApiResponse.success(getMessage("success.hold.api"), response));
    }

    @Operation(
            summary = "Giải phóng tiền giữ (Release Hold)",
            description = "API giải phóng số tiền đã giữ tạm thời, số tiền sẽ quay lại trạng thái khả dụng."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giải phóng tiền giữ thành công",
                    content = @Content(schema = @Schema(implementation = BalanceOperationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Số tiền giữ không đủ để giải phóng"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy tài khoản"
            )
    })
    @PostMapping("/release-hold")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> releaseHoldAmount(
            @Parameter(description = "Số tài khoản cần giải phóng tiền giữ", required = true, example = "37029832305730")
            @RequestParam String accountNumber,
            @Parameter(description = "Số tiền cần giải phóng (VND)", required = true, example = "100000.00")
            @RequestParam BigDecimal amount,
            @Parameter(description = "Mã tham chiếu giao dịch (UUID)", required = true, example = "TX-20241214-001")
            @RequestParam String transactionReference) {
        BalanceOperationResponse response = balanceManagementService.releaseHoldAmount(
                accountNumber, amount, transactionReference);
        return ResponseEntity.ok(ApiResponse.success(getMessage("success.release.hold.api"), response));
    }

    private String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }
}

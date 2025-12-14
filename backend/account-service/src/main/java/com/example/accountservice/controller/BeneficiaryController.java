package com.example.accountservice.controller;

import com.example.accountservice.dto.request.CreateBeneficiaryRequest;
import com.example.accountservice.dto.request.UpdateBeneficiaryRequest;
import com.example.accountservice.dto.response.BeneficiaryResponse;
import com.example.accountservice.service.BeneficiaryService;
import com.example.commonapi.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing beneficiaries (danh bạ thụ hưởng)
 */
@Tag(name = "Beneficiary Management", description = "APIs quản lý danh bạ thụ hưởng (tạo, sửa, xóa, tìm kiếm người thụ hưởng)")
@RestController
@RequestMapping("/api/customers/{customerId}/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    /**
     * Create a new beneficiary
     */
    @Operation(
        summary = "Tạo mới người thụ hưởng",
        description = "Thêm người thụ hưởng mới vào danh bạ của khách hàng."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo người thụ hưởng thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> createBeneficiary(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin người thụ hưởng mới",
                required = true
            )
            @RequestBody @Valid CreateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.createBeneficiary(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created successfully", response));
    }

    /**
     * Update beneficiary details
     */
    @Operation(
        summary = "Cập nhật thông tin người thụ hưởng",
        description = "Cập nhật nickname, số tài khoản hoặc thông tin khác của người thụ hưởng."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người thụ hưởng"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PutMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiary(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "ID người thụ hưởng", example = "BEN-2024-0001")
            @PathVariable String beneficiaryId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cần cập nhật",
                required = true
            )
            @RequestBody @Valid UpdateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(customerId, beneficiaryId, request);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", response));
    }

    /**
     * Delete a beneficiary
     */
    @Operation(
        summary = "Xóa người thụ hưởng",
        description = "Xóa người thụ hưởng khỏi danh bạ."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người thụ hưởng"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "ID người thụ hưởng", example = "BEN-2024-0001")
            @PathVariable String beneficiaryId) {
        beneficiaryService.deleteBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }

    /**
     * Get beneficiary by ID
     */
    @Operation(
        summary = "Lấy thông tin người thụ hưởng theo ID",
        description = "Xem chi tiết một người thụ hưởng cụ thể."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người thụ hưởng"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getBeneficiary(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "ID người thụ hưởng", example = "BEN-2024-0001")
            @PathVariable String beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary fetched successfully", response));
    }

    /**
     * Get all beneficiaries (no pagination)
     */
    @Operation(
        summary = "Lấy tất cả người thụ hưởng",
        description = "Lấy danh sách tất cả người thụ hưởng của khách hàng (không phân trang)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAllBeneficiaries(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId) {
        List<BeneficiaryResponse> response = beneficiaryService.getAllBeneficiaries(customerId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries fetched successfully", response));
    }

    /**
     * Get beneficiaries with pagination
     */
    @Operation(
        summary = "Lấy danh sách người thụ hưởng có phân trang",
        description = "Lấy danh sách người thụ hưởng với phân trang. Mặc định sắp xếp theo ngày chuyển gần nhất."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<BeneficiaryResponse>>> getBeneficiariesPaged(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "Thông tin phân trang (size, page, sort)")
            @PageableDefault(size = 20, sort = "lastTransferDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BeneficiaryResponse> response = beneficiaryService.getBeneficiaries(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries fetched successfully", response));
    }

    /**
     * Search beneficiaries by name or nickname
     */
    @Operation(
        summary = "Tìm kiếm người thụ hưởng",
        description = "Tìm kiếm người thụ hưởng theo tên hoặc nickname."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> searchBeneficiaries(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "Từ khóa tìm kiếm (tên hoặc nickname)", example = "Nguyen")
            @RequestParam String q) {
        List<BeneficiaryResponse> response = beneficiaryService.searchBeneficiaries(customerId, q);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response));
    }

    /**
     * Get most frequently used beneficiaries
     */
    @Operation(
        summary = "Lấy danh sách người thụ hưởng được sử dụng nhiều nhất",
        description = "Lấy top N người thụ hưởng có số lần chuyển tiền nhiều nhất."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/most-used")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getMostUsedBeneficiaries(
            @Parameter(description = "ID khách hàng", example = "CUST-2024-0001")
            @PathVariable String customerId,
            @Parameter(description = "Số lượng kết quả tối đa", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        List<BeneficiaryResponse> response = beneficiaryService.getMostUsedBeneficiaries(customerId, limit);
        return ResponseEntity.ok(ApiResponse.success("Most used beneficiaries fetched successfully", response));
    }
}

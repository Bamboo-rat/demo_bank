package com.example.customerservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cập nhật thông tin khách hàng")
public class CustomerUpdateRequest {
    @Schema(description = "Email mới", example = "newemail@example.com")
    private String email;
    
    @Schema(description = "Số điện thoại mới", example = "0987654321")
    private String phone;
    
    @Schema(description = "Địa chỉ tạm trú mới")
    @Valid
    private AddressRequest temporaryAddress;
}

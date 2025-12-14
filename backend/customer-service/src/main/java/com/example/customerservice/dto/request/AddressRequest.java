package com.example.customerservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin địa chỉ")
public class AddressRequest {
    @Schema(description = "Số nhà, tên đường", example = "123 Nguyễn Trãi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Số nhà, tên đường không được để trống")
    private String street;

    @Schema(description = "Phường/Xã", example = "Phường Thanh Xuân", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @Schema(description = "Quận/Huyện", example = "Quận Thanh Xuân", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;

    @Schema(description = "Quốc gia", example = "Việt Nam", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Country cannot be blank")
    private String country;
}

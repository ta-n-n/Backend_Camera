package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraLocationCreateRequest {

    @NotBlank(message = "Tên vị trí không được để trống")
    @Size(max = 100, message = "Tên vị trí không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private Double latitude;

    private Double longitude;

    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    private String city;

    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String district;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
}

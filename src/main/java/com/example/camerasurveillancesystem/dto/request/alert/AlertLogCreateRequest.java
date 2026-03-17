package com.example.camerasurveillancesystem.dto.request.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertLogCreateRequest {

    @NotNull(message = "Alert ID is required")
    private Long alertId;

    @NotBlank(message = "Action type is required")
    private String actionType; // CREATED, ACKNOWLEDGED, ASSIGNED, STATUS_CHANGED, RESOLVED, COMMENT

    private String comment; // Bình luận/ghi chú

    private String previousValue; // Giá trị trước khi thay đổi (cho tracking)

    private String newValue; // Giá trị mới
}

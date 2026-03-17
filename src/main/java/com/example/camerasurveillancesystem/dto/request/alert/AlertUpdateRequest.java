package com.example.camerasurveillancesystem.dto.request.alert;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // NEW, ACKNOWLEDGED, INVESTIGATING, RESOLVED, FALSE_POSITIVE

    private String severity; // Update severity if needed

    private String assignedToUserId; // Assign to user

    private String resolutionNotes; // Ghi chú xử lý

    private String metadata; // Update metadata
}

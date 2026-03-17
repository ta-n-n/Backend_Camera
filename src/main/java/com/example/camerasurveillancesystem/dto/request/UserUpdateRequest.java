package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateRequest {

    private String email;

    private String fullName;

    private String phoneNumber;

    private Set<Long> roleIds;

    private Boolean enabled;

    private Boolean locked;
}

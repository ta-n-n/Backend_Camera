package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.*;
import com.example.camerasurveillancesystem.dto.response.AuthResponse;
import com.example.camerasurveillancesystem.dto.response.UserResponse;

public interface AuthService {

    /**
     * User login
     */
    AuthResponse login(LoginRequest request);

    /**
     * User registration
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Refresh access token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout
     */
    void logout(String token);

    /**
     * Get current user profile
     */
    UserResponse getCurrentUser();

    /**
     * Change password
     */
    void changePassword(ChangePasswordRequest request);
}

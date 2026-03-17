package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.UserCreateRequest;
import com.example.camerasurveillancesystem.dto.request.UserUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Get all users with pagination
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Get user by ID
     */
    UserResponse getUserById(Long id);

    /**
     * Create new user
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Update user
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * Delete user
     */
    void deleteUser(Long id);

    /**
     * Enable/Disable user
     */
    void toggleUserStatus(Long id);

    /**
     * Lock/Unlock user
     */
    void toggleUserLock(Long id);

    /**
     * Reset user password
     */
    void resetPassword(Long id, String newPassword);
}

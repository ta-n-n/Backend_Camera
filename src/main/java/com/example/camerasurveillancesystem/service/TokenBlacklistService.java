package com.example.camerasurveillancesystem.service;

public interface TokenBlacklistService {
    
    /**
     * Add token to blacklist
     */
    void blacklistToken(String token, String username, String reason);
    
    /**
     * Check if token is blacklisted
     */
    boolean isTokenBlacklisted(String token);
    
    /**
     * Cleanup expired tokens
     */
    void cleanupExpiredTokens();
}

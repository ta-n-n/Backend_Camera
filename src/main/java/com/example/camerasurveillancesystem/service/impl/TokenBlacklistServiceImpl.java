package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.TokenBlacklist;
import com.example.camerasurveillancesystem.repository.TokenBlacklistRepository;
import com.example.camerasurveillancesystem.security.JwtUtil;
import com.example.camerasurveillancesystem.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void blacklistToken(String token, String username, String reason) {
        try {
            // Extract expiration from token
            Date expiration = jwtUtil.extractExpiration(token);
            LocalDateTime expiryDate = expiration.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            TokenBlacklist blacklist = new TokenBlacklist();
            blacklist.setToken(token);
            blacklist.setUsername(username);
            blacklist.setExpiryDate(expiryDate);
            blacklist.setReason(reason);

            tokenBlacklistRepository.save(blacklist);
            log.info("Token blacklisted for user: {} (reason: {})", username, reason);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired blacklisted tokens...");
        try {
            tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Expired tokens cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens: {}", e.getMessage());
        }
    }
}

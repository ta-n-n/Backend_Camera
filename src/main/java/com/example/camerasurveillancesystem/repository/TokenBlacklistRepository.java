package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    /**
     * Check if token is blacklisted
     */
    boolean existsByToken(String token);
    
    /**
     * Delete expired tokens (cleanup)
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    /**
     * Count blacklisted tokens
     */
    long countByUsername(String username);
}

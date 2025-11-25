package com.team3.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 인증 토큰 모델
 * <p>
 * 사용자 로그인 후 발급되는 세션 토큰
 * </p>
 */
public class AuthToken {
    
    private final String token;
    private final String userId;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    
    public AuthToken(String userId, int validMinutes) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = issuedAt.plusMinutes(validMinutes);
    }
    
    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters
    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
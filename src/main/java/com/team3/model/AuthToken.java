package com.team3.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 인증 토큰 모델
 * <p>
 * 사용자 로그인 후 발급되는 세션 토큰
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-25
 */
public class AuthToken {
    
    private final String token;
    private final String userId;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    
    /**
     * 인증 토큰 모델 생성자
     * 
     * @param userId 사용자 아이디
     * @param validMinutes 토큰의 유효 지속 시간(분)
     */
    public AuthToken(String userId, int validMinutes) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = issuedAt.plusMinutes(validMinutes);
    }
    
    /**
     * 토큰이 만료되었는지 확인한다.
     * 
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
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
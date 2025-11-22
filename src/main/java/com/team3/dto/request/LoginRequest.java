package com.team3.dto.request;

/**
 * 로그인 요청 DTO
 * <p>
 * 사용자 ID와 해싱된 비밀번호를 서버에 전달하기 위한 데이터 객체.
 * 순수하게 데이터만 담으며, 비즈니스 로직은 포함하지 않는다.
 * </p>
 * 
 * @apiNote 비밀번호는 반드시 BCrypt로 해싱된 값이어야 한다.
 */
public class LoginRequest {
    private final String userId;
    private final String password;
    
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
    
    public String getUserId() { return userId; }
    public String getPassword() { return password; }
}
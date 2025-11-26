package com.team3.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자 도메인 모델
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class User {

    public enum Role{
        ADMIN,
        CSR
    }
    
    private String userId;              // 사용자 아이디
    private String password;            // 사용자 비밀번호(평문)
    private String userName;            // 사용자 이름
    private Role role;                  // 사용자 역할
    private LocalDateTime createdAt;    // 사용자 생성 시간
    private LocalDateTime updatedAt;    // 사용자 업데이트 시간
    
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String userId, String password, String userName, Role role) {
        this();
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { 
        this.userId = userId; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { 
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { 
        this.userName = userName;
        this.updatedAt = LocalDateTime.now();
    }

    public Role getRole() { return role; }
    public void setRole(Role role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
package com.team3.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 고객 정보 도메인 모델 (SFR-700)
 * @since 2025-11-29
 */
public class Customer {
    private String id;              // 고객 고유 ID (자동 생성)
    private String name;            // 고객 이름 (SFR-701)
    private String phoneNumber;     // 전화번호 (SFR-702)
    private String roomNumber;      // 투숙 객실 번호 (조회용, SFR-703)
    private String feedback;        // 고객 피드백 (SFR-704)
    private String createdAt;       // 등록일

    public Customer() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now().toString();
    }

    public Customer(String name, String phoneNumber, String roomNumber, String feedback) {
        this();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roomNumber = roomNumber;
        this.feedback = feedback;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getCreatedAt() { return createdAt; }

    public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
}
}
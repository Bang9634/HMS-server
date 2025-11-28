package com.team3.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 예약 도메인 모델
 * * @author bang9634
 * @since 2025-11-28
 */
public class Reservation {

    private String id;              // 예약 고유 ID
    private String userId;          // 예약한 사용자 ID
    private String roomId;          // 객실 ID
    private String guestName;       // 투숙객 이름
    private String phone;           // 연락처
    private String checkInDate;     // 체크인 날짜 (YYYY-MM-DD)
    private String checkOutDate;    // 체크아웃 날짜 (YYYY-MM-DD)
    private int guestCount;         // 인원 수
    private LocalDateTime createdAt; // 예약 생성 시간
    private LocalDateTime updatedAt; // 예약 업데이트 시간

    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Reservation(String userId, String roomId, String guestName, String phone, String checkInDate, String checkOutDate, int guestCount) {
        this();
        this.id = UUID.randomUUID().toString(); // ID 자동 생성
        this.userId = userId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { 
        this.id = id; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { 
        this.userId = userId; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { 
        this.roomId = roomId; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { 
        this.guestName = guestName; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { 
        this.checkInDate = checkInDate; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { 
        this.checkOutDate = checkOutDate; 
        this.updatedAt = LocalDateTime.now();
    }

    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { 
        this.guestCount = guestCount; 
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
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", guestName='" + guestName + '\'' +
                ", checkInDate='" + checkInDate + '\'' +
                ", checkOutDate='" + checkOutDate + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
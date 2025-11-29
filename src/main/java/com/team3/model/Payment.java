package com.team3.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [결제 데이터 모델]
 * * 파일에 저장될 데이터의 모양을 정의하는 클래스
 * 이 객체 하나가 JSON 파일의 한 줄({})이 됨.
 * * PaymentRequest를 통합하여 요청 데이터와 저장 데이터를 모두 처리함.
 * * @author 김현준
 */
public class Payment {
    // --- [1. 클라이언트 요청(Request) 데이터] ---
    private String guestName;       // 고객명
    private int roomCharge;         // 객실료 (요청)
    private int foodCharge;         // 식음료 (요청)
    private String method;          // 결제 수단 (CARD, CASH)
    private String cardNumber;      // 카드 번호

    // --- [2. 서버에서 생성/계산하는 데이터] ---
    private int totalAmount;        // 총 결제 금액 (계산됨)
    private String details;         // 상세 내용 (계산됨)
    private String paymentTime;     // 결제 승인 시간 (자동생성)

    // 기본 생성자 (Gson용)
    public Payment() {}

    /**
     * 데이터를 완성하는 메서드 (Service에서 호출)
     * - 총 금액 계산
     * - 상세 내용(details) 생성
     * - 결제 시간 기록
     */
    public void calculateAndFinalize() {
        // 1. 총 금액 계산
        this.totalAmount = this.roomCharge + this.foodCharge;

        // 2. 결제 타입 구분 (워크인 vs 투숙객)
        if (this.roomCharge == 0 && this.foodCharge > 0) {
            this.details = "Walk-in(Food)"; // 객실료가 0원이면 워크인
        } else if (this.roomCharge > 0 && this.foodCharge > 0) {
            this.details = "Room+Food";     // 둘 다 있으면 통합 결제
        } else {
            this.details = "RoomOnly";      // 기본값
        }

        // 3. 카드 번호 처리 ("N/A")
        if (this.cardNumber == null || this.cardNumber.isEmpty()) {
            this.cardNumber = "N/A";
        }

        // 4. 현재 시간 저장
        this.paymentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getter
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
    public int getTotalAmount() { return totalAmount; }
    public String getDetails() { return details; }
    public String getPaymentTime() { return paymentTime; }

    @Override
    public String toString() {
        return String.format("[결제] 시간:%s | 이름:%s | 금액:%d원 | 타입:%s", paymentTime, guestName, totalAmount, details);
    }
}
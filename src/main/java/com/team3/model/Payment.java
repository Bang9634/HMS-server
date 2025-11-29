package com.team3.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 결제 정보를 담는 핵심 도메인 모델 클래스
 * <p>
 * 클라이언트의 요청 데이터와 서버의 저장 데이터를 모두 관리한다.
 * 영수증 번호(receiptId)를 고유 식별자로 사용한다.
 * </p>
 * @author 김현준
 */
public class Payment {
    // 식별자
    private String receiptId;       // 영수증 번호 (예: 251129-143000)
    private String reservationId;   // 예약 번호 (없으면 Walk-in)

    // 결제 데이터
    private String guestName;
    private int roomCharge;         // 객실료
    private int foodCharge;         // 식음료료
    private int totalAmount;        // 합계
    private String method;          // CARD / CASH
    private String cardNumber;      // 카드 번호
    private String details;         // 요약 정보
    private String paymentTime;     // 승인 일시

    public Payment() {}

    /**
     * 클라이언트 요청 데이터를 기반으로 객체 생성
     */
    public Payment(String guestName, int roomCharge, int foodCharge, String method, String cardNumber, String reservationId) {
        // 영수증 번호 자동 생성 (년월일-시분초)
        this.receiptId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmmss"));
        
        this.reservationId = (reservationId == null || reservationId.trim().isEmpty()) ? "Walk-in" : reservationId;
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodCharge = foodCharge;
        this.method = method;
        this.cardNumber = cardNumber;
        this.totalAmount = roomCharge + foodCharge;
    }

    /**
     * 데이터 저장 전 최종 계산 및 포맷팅 (Service에서 호출)
     */
    public void calculateAndFinalize() {
        if (this.receiptId == null) {
            this.receiptId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmmss"));
        }
        
        if (this.reservationId == null || this.reservationId.trim().isEmpty()) {
            this.reservationId = "Walk-in";
        }

        this.totalAmount = this.roomCharge + this.foodCharge;

        // 상세 내역 타입 결정
        if (this.roomCharge == 0 && this.foodCharge > 0) this.details = "Walk-in(Food)";
        else if (this.roomCharge > 0 && this.foodCharge > 0) this.details = "Room+Food";
        else this.details = "RoomOnly";

        if (this.cardNumber == null || this.cardNumber.isEmpty()) {
            this.cardNumber = "N/A";
        }
        
        this.paymentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters
    public String getReceiptId() { return receiptId; }
    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public int getTotalAmount() { return totalAmount; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
    public String getDetails() { return details; }
    public String getPaymentTime() { return paymentTime; }
}
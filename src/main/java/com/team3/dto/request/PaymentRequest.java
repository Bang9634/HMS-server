package com.team3.dto.request;

/**
 * PaymentRequest: 클라이언트(프론트)에서 넘어오는 결제 요청 데이터
 * - JSON으로 바꾸기 전, 순수 자바 객체 상태
 * - 결제 요청 데이터 - 누가, 얼마를, 어떻게
 */
public class PaymentRequest {
    private String guestName;       // 고객명
    private int roomCharge;         // 객실료 (워크인이면 0)
    private int foodCharge;         // 식음료료
    private String method;          // CARD 또는 CASH
    private String cardNumber;      // 카드번호 (현금이면 null)

    public PaymentRequest(String guestName, int roomCharge, int foodCharge, String method, String cardNumber) {
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodCharge = foodCharge;
        this.method = method;
        this.cardNumber = cardNumber;
    }

    // Getter 메서드들
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
}
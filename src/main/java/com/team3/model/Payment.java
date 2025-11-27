package com.team3.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [결제 데이터 모델]
 * * 파일에 저장될 데이터의 모양을 정의하는 클래스
 * 이 객체 하나가 JSON 파일의 한 줄({})이 됨.
 */
public class Payment {
    private String guestName;       // 고객명
    private int totalAmount;        // 총 결제 금액
    private String details;         // 상세 내용 (워크인, 룸온리 등)
    private String method;          // 결제 수단 (CARD, CASH)
    private String cardNumber;      // 카드 번호
    private String paymentTime;     // 결제 승인 시간

    // 생성자
    public Payment(String guestName, int totalAmount, String details, String method, String cardNumber) {
        this.guestName = guestName;
        this.totalAmount = totalAmount;
        this.details = details;
        this.method = method;
        
        // 카드 번호가 없으면 "N/A"(해당없음)으로 저장, 있으면 그대로 저장
        this.cardNumber = (cardNumber == null || cardNumber.isEmpty()) ? "N/A" : cardNumber;
        
        // 현재 시간을 "년-월-일 시:분:초" 형태로 자동 저장
        this.paymentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    // toString(): 나중에 System.out.println(payment) 했을 때 예쁘게 나오게
    @Override
    public String toString() {
        return String.format("[결제] 시간:%s | 이름:%s | 금액:%d원 | 타입:%s", paymentTime, guestName, totalAmount, details);
    }
}
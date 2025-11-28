package com.team3.service;

import com.team3.dto.request.PaymentRequest;
import com.team3.model.Payment;
import com.team3.repository.PaymentRepository;
import java.util.List;

/**
 * [결제 서비스]
 * * 비즈니스 로직(업무 규칙)을 처리
 * - 카드가 유효한지 검사
 * - 금액 계산
 * - 워크인 손님인지 투숙객인지 구분
 * - 최종적으로 리포지토리에게 저장 시킴
 * @author 김현준
 */
public class PaymentService {
    
    private final PaymentRepository paymentRepository;

    /**
     * Main.java에서 미리 만들어둔 리포지토리를 파라미터로 건네받아 사용
     * -> 데이터가 꼬이지 않고 하나로 관리됨.
     */
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 요청을 처리하는 핵심 메서드
     * @param req 클라이언트(화면)에서 보낸 결제 요청 데이터
     */
    public void processPayment(PaymentRequest req) {
        // 1. 카드 유효성 검사 (카드로 결제할 때만)
        if ("CARD".equalsIgnoreCase(req.getMethod())) {
            String cNum = req.getCardNumber();
            // 번호가 없거나, 하이픈(-) 빼고 16자리가 안 되면 가짜 카드로 판단
            if (cNum == null || cNum.replaceAll("-", "").length() < 16) {
                // 에러 -> PaymentHandler: 400 Bad Request를 보냄.
                throw new IllegalArgumentException("유효하지 않은 카드 번호입니다.");
            }
        }

        // 2. 총 금액 계산 (객실료 + 식음료)
        int total = req.getRoomCharge() + req.getFoodCharge();
        
        // 3. 결제 타입 구분 (워크인 vs 투숙객)
        String detailType = "RoomOnly"; // 기본값
        
        if (req.getRoomCharge() == 0 && req.getFoodCharge() > 0) {
            detailType = "Walk-in(Food)"; // 객실료가 0원이면 워크인
        } else if (req.getRoomCharge() > 0 && req.getFoodCharge() > 0) {
            detailType = "Room+Food";     // 둘 다 있으면 통합 결제
        }

        // 4. 저장할 Payment 객체(모델) 생성
        Payment newPayment = new Payment(
            req.getGuestName(),
            total,
            detailType,
            req.getMethod(),
            req.getCardNumber()
        );

        // 5. 리포지토리를 통해 파일에 저장
        paymentRepository.save(newPayment);
        
        // 서버 콘솔에 로그 출력
        System.out.println(">> [Service] 결제 승인 완료: 고객명(" + req.getGuestName() + ")");
    }

    /**
     * 전체 결제 내역을 조회하는 메서드
     * PaymentHandler에 필요.
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    /*
    * 결제 내역 삭제 기능
    */
    public void clearAllHistory(){
        paymentRepository.deleteAll();
    }
    
}
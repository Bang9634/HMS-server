package com.team3.service;

import com.team3.model.Payment;
import com.team3.repository.PaymentRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [결제 서비스]
 * * 비즈니스 로직(업무 규칙)을 처리하는 클래스
 * - 카드가 유효한지 검사
 * - 금액 계산 및 데이터 완성 (영수증 번호 생성 등)
 * - 워크인 손님인지 투숙객인지 구분
 * - 최종적으로 리포지토리에게 저장을 지시함
 * * @author 김현준
 */
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;

    /**
     * Main.java에서 미리 만들어둔 리포지토리를 파라미터로 건네받아 사용
     * -> 데이터가 꼬이지 않고 하나로 관리됨. (의존성 주입)
     */
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 요청을 처리하는 핵심 메서드
     * 파라미터가 PaymentRequest -> Payment 로 변경됨 (모델 통합)
     * @param payment 클라이언트에서 넘어온 결제 정보 객체
     */
    public void processPayment(Payment payment) {
        // 1. 카드 유효성 검사 (카드로 결제할 때만)
        if ("CARD".equalsIgnoreCase(payment.getMethod())) {
            String cNum = payment.getCardNumber();
            // 번호가 없거나, 하이픈(-) 빼고 16자리가 안 되면 가짜 카드로 판단
            if (cNum == null || cNum.replaceAll("-", "").length() < 16) {
                // 에러 -> PaymentHandler: 400 Bad Request를 보냄.
                throw new IllegalArgumentException("유효하지 않은 카드 번호입니다.");
            }
        }

        // 2. 데이터 완성 (금액 계산, 시간 기록, 영수증 번호 생성 등)
        // 기존에는 Service에서 new Payment()를 했지만, 이제는 넘어온 객체를 완성시킵니다.
        payment.calculateAndFinalize();

        // 3. 리포지토리를 통해 파일에 저장
        paymentRepository.save(payment);
        
        // 서버 로그 출력
        logger.info("[Service] 결제 승인 완료: 고객명({}) / 영수증({})", 
                payment.getGuestName(), payment.getReceiptId());
    }

    /**
     * 전체 결제 내역을 조회하는 메서드
     * PaymentHandler가 GET 요청을 받으면 호출함.
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    /*
     * 결제 내역 전체 삭제 기능 (초기화)
     */
    public void clearAllHistory(){
        paymentRepository.deleteAll();
    }
    
    /*
     * [선택 삭제] 영수증 번호(Receipt ID)로 특정 내역 삭제
     * Handler에서 ID를 추출해서 넘겨주면, Repository에게 삭제를 명령함.
     */
    public void deleteByReceiptId(String receiptId) {
        paymentRepository.deleteByReceiptId(receiptId);
    }
}
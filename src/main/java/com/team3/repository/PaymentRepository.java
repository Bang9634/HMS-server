package com.team3.repository;

import com.team3.model.Payment;
import java.util.List;

/**
 * 결제 저장소 인터페이스
 * @author 김현준
 */
public interface PaymentRepository {
    // 저장
    void save(Payment payment);
    
    // 전체 조회
    List<Payment> findAll();
    
    // 전체 삭제
    void deleteAll();
    
    // 영수증 번호로 선택 삭제
    void deleteByReceiptId(String receiptId);
}
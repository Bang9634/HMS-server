package com.team3.repository;

import com.team3.model.Payment;
import java.util.List;

/**
 * [결제 저장소 인터페이스]
 * @author 김현준
 */

// class가 아닌 interface
public interface PaymentRepository {
    
    // 결제 정보를 저장소에 저장하라고 명령하는 메서드
    void save(Payment payment);
    
    // 저장소에 있는 모든 결제 내역을 가져오라고 명령하는 메서드
    List<Payment> findAll();
    
    // 모든 결제 내역 삭제(초기화)
    void deleteAll();
    
    // 원하는 결제 내역(이름 검색) 삭제
    void deleteByGuestName(String guestName);
}
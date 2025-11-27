package com.team3.repository;

import com.team3.model.Payment;
import java.util.List;

/**
 * [결제 저장소 인터페이스]
 * 나중에 파일 저장이 아니라 진짜 DB로 바꾸더라도
 * Service 코드는 건드리지 않고 이 껍데기의 구현체만 갈아끼우기 위해서 사용.
 */

// 따라서 class가 아닌 interface가 되어야함!
public interface PaymentRepository {
    
    // 결제 정보를 저장소에 저장하라고 명령하는 메서드
    void save(Payment payment);
    
    // 저장소에 있는 모든 결제 내역을 가져오라고 명령하는 메서드
    List<Payment> findAll();
}
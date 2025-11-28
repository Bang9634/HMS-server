package com.team3.repository;

import java.util.List;
import java.util.Optional;

import com.team3.model.Reservation;

/**
 * 예약 데이터 저장소 인터페이스
 * * @author bang9634
 * @since 2025-11-28
 */
public interface ReservationRepository {
    
    /**
     * 예약 저장
     * * @param reservation 저장할 예약 객체
     */
    boolean save(Reservation reservation);

    /**
     * 예약 업데이트
     * * @param reservation 업데이트할 예약 객체
     */
    boolean update(Reservation reservation);
    
    /**
     * ID로 예약 찾기
     * * @param id 탐색할 예약 아이디
     */
    Optional<Reservation> findById(String id);
    
    /**
     * 모든 예약 조회
     * * @return 모든 예약 객체를 List에 저장해 반환한다.
     */
    List<Reservation> findAll();
    
    /**
     * 사용자 ID로 예약 목록 조회
     * * @param userId 조회할 사용자 아이디
     */
    List<Reservation> findByUserId(String userId);
    
    /**
     * 예약 삭제
     * * @param id 삭제할 예약 아이디
     * @return 삭제 성공 여부를 true, false로 반환한다.
     */
    boolean deleteById(String id);
    
    /**
     * ID 존재 여부 확인
     * * @param id 존재 여부를 확인할 예약 아이디
     * @return 존재 여부를 true, false로 반환한다.
     */
    boolean existsById(String id);
}
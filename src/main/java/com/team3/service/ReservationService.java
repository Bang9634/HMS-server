package com.team3.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.Reservation;
import com.team3.repository.ReservationRepository;

/**
 * 예약 관련 비즈니스 로직을 처리하는 서비스 클래스
 * * @author bang9634
 * @since 2025-11-28
 */
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        if (reservationRepository == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.reservationRepository = reservationRepository;
    }

    /**
     * 예약을 생성한다.
     * * @param reservation 생성할 예약 객체
     * @return 성공 여부
     */
    public boolean createReservation(Reservation reservation) {
        logger.info("예약 생성 시도: roomId={}", reservation.getRoomId());

        if (reservation.getRoomId() == null || reservation.getGuestName() == null) {
            logger.warn("필수 정보 누락");
            throw new IllegalArgumentException("객실 정보와 예약자명은 필수입니다.");
        }

        // ID가 없으면 자동 생성
        if (reservation.getId() == null) {
            reservation.setId(UUID.randomUUID().toString());
        }

        return reservationRepository.save(reservation);
    }

    /**
     * 모든 예약 정보를 반환한다.
     * * @return 모든 예약 정보
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * 예약 ID로 예약을 삭제한다.
     * * @param id 삭제할 예약 ID
     * @return 성공 여부
     */
    public boolean cancelReservation(String id) {
        logger.info("예약 취소 요청: id={}", id);
        return reservationRepository.deleteById(id);
    }
    
    /**
     * 예약 정보를 수정한다. (SFR-304)
     * * @param reservation 수정할 정보가 담긴 객체 (ID 필수)
     * @return 수정 성공 여부
     */
    public boolean updateReservation(Reservation reservation) {
        logger.info("예약 수정 요청: id={}", reservation.getId());
        
        // 1. 존재하는 예약인지 확인
        if (!reservationRepository.existsById(reservation.getId())) {
            logger.warn("수정 실패: 존재하지 않는 예약 ID");
            throw new IllegalArgumentException("존재하지 않는 예약입니다.");
        }

        // 2. 필수 값 검증 (객실, 이름 등은 비어있으면 안 됨)
        if (reservation.getRoomId() == null || reservation.getGuestName() == null) {
             throw new IllegalArgumentException("필수 정보가 누락되었습니다.");
        }

        // 3. 업데이트 수행 (Repo의 update 메서드 호출)
        // 기존의 createdAt 등은 유지하고, 변경된 정보만 덮어쓰도록 로직 보완 가능하지만,
        // 여기서는 Repository 구현체가 덮어쓰기 방식이므로 그대로 전달.
        return reservationRepository.update(reservation);
    }
}

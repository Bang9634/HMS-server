package com.team3.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.Reservation;
import com.team3.util.JsonFileManager;

/**
 * JSON 파일 기반 예약 저장소 구현체
 * <p>
 * JsonFileManager를 사용하여 reservations.json 파일에 예약 데이터를 저장한다.
 * </p>
 * * @author bang9634
 * @since 2025-11-28
 */
public class JsonReservationRepository implements ReservationRepository {
    private static final Logger logger = LoggerFactory.getLogger(JsonReservationRepository.class);
    private final JsonFileManager<Reservation> fileManager;

    /**
     * JsonReservationRepository 생성자
     * * @param jsonFileManager 의존성 주입용 jsonFileManager 객체
     */
    public JsonReservationRepository(JsonFileManager<Reservation> jsonFileManager) {
        logger.info("JsonReservationRepository 초기화 시작...");
        this.fileManager = jsonFileManager;
        logger.info("JsonReservationRepository 초기화 완료");
    }

    @Override
    public boolean save(Reservation reservation) {
        logger.info("예약 저장: {}", reservation.getId());
        List<Reservation> list = fileManager.readAll();
        list.add(reservation);
        
        fileManager.writeAll(list);
        return true;
    }

    @Override
    public boolean update(Reservation reservation) {
        logger.info("예약 업데이트: {}", reservation.getId());
        List<Reservation> list = fileManager.readAll();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(reservation.getId())) {
                list.set(i, reservation);
                logger.debug("기존 예약 업데이트: {}", reservation.getId());
                fileManager.writeAll(list);
                return true;
            }
        }
        logger.debug("업데이트할 예약이 존재하지 않음: {}", reservation.getId());
        return false;
    }

    @Override
    public Optional<Reservation> findById(String id) {
        logger.debug("예약 조회: id={}", id);
        return fileManager.readAll().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        logger.debug("전체 예약 조회");
        return fileManager.readAll();
    }

    @Override
    public List<Reservation> findByUserId(String userId) {
        logger.debug("사용자 예약 조회: userId={}", userId);
        return fileManager.readAll().stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        logger.info("예약 삭제: {}", id);
        List<Reservation> list = fileManager.readAll();
        boolean removed = list.removeIf(r -> r.getId().equals(id));
        
        if (removed) {
            fileManager.writeAll(list);
            logger.info("예약 삭제 완료: {}", id);
            return true;
        }
        
        logger.warn("삭제할 예약 없음: {}", id);
        return false;
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}
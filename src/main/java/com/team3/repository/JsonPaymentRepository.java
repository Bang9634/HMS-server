package com.team3.repository;

import com.team3.model.Payment;
import com.team3.util.JsonFileManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 파일 기반 결제 저장소 구현체
 * <p>
 * PaymentRepository 인터페이스를 구현하여 실제 파일 I/O를 수행한다.
 * JsonFileManager를 사용하여 데이터를 읽고 쓴다.
 * </p>
 * * @author 김현준
 */
public class JsonPaymentRepository implements PaymentRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonPaymentRepository.class);
    private final JsonFileManager<Payment> fileManager;

    public JsonPaymentRepository(JsonFileManager<Payment> fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void save(Payment payment) {
        try {
            List<Payment> payments = fileManager.readAll(); 
            if (payments == null) {
                payments = new ArrayList<>();
            }
            payments.add(payment);
            fileManager.writeAll(payments); 
        } catch (Exception e) {
            // [수정] 로그 기록 후 런타임 예외로 던져서 상위(Service/Handler)가 알게 함
            logger.error("결제 정보 저장 실패", e);
            throw new RuntimeException("Save failed", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        try {
            List<Payment> payments = fileManager.readAll();
            return payments != null ? payments : new ArrayList<>();
        } catch (Exception e) {
            logger.error("결제 내역 조회 실패", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteAll() {
        try {
            fileManager.writeAll(new ArrayList<>());
            logger.info("저장소 데이터 전체 삭제됨");
        } catch (Exception e) {
            logger.error("데이터 전체 삭제 실패", e);
            throw new RuntimeException("Delete all failed", e);
        }
    }
    
    @Override
    public void deleteByGuestName(String guestName) {
        try {
            List<Payment> payments = fileManager.readAll();
            if (payments != null) {
                // 자바의 removeIf 기능을 써서 이름이 같은 걸 다 지움
                payments.removeIf(p -> p.getGuestName().equals(guestName));
                fileManager.writeAll(payments);
            }
        } catch (Exception e) {
            throw new RuntimeException("Delete specific failed", e);
        }
    }
    
}
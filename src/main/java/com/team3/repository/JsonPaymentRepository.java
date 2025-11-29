package com.team3.repository;

import com.team3.model.Payment;
import com.team3.util.JsonFileManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 파일 기반 결제 저장소 구현체
 * @author 김현준
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
            if (payments == null) payments = new ArrayList<>();
            payments.add(payment);
            fileManager.writeAll(payments);
        } catch (Exception e) {
            logger.error("저장 실패", e);
            throw new RuntimeException("Save failed", e);
        }
    }
    
    // 조회
    @Override
    public List<Payment> findAll() {
        try {
            List<Payment> payments = fileManager.readAll();
            return payments != null ? payments : new ArrayList<>();
        } catch (Exception e) {
            logger.error("조회 실패", e);
            return new ArrayList<>();
        }
    }
    
    // 전체 초기화
    @Override
    public void deleteAll() {
        try {
            fileManager.writeAll(new ArrayList<>());
        } catch (Exception e) {
            logger.error("전체 삭제 실패", e);
            throw new RuntimeException("DeleteAll failed", e);
        }
    }

    // 영수증 번호로 삭제
    @Override
    public void deleteByReceiptId(String receiptId) {
        try {
            List<Payment> payments = fileManager.readAll();
            if (payments != null) {
                boolean removed = payments.removeIf(p -> p.getReceiptId().equals(receiptId));
                if (removed) {
                    fileManager.writeAll(payments);
                    logger.info("삭제 성공 ReceiptId: {}", receiptId);
                } else {
                    throw new RuntimeException("해당 영수증 번호를 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            logger.error("개별 삭제 실패", e);
            throw new RuntimeException("Delete specific failed", e);
        }
    }
}
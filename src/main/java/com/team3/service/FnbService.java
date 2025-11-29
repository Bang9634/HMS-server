package com.team3.service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.model.FnbItem;
import com.team3.repository.FnbRepository;

public class FnbService {
    private static final Logger logger = LoggerFactory.getLogger(FnbService.class);
    private final FnbRepository fnbRepository;

    public FnbService(FnbRepository fnbRepository) {
        this.fnbRepository = fnbRepository;
    }

    public boolean addFnbItem(FnbItem item) {
        logger.info("F&B 추가 요청: 메뉴={}, 결제={}", item.getMenuName(), item.getPaymentMethod());

        // SFR-803: 객실 청구 검증
        if (item.getPaymentMethod() == FnbItem.PaymentMethod.ROOM_CHARGE) {
            if (item.getRoomId() == null || item.getRoomId().trim().isEmpty()) {
                throw new IllegalArgumentException("객실 청구 시 '객실 번호'는 필수입니다.");
            }
        }
        
        // 총액 자동 계산
        if (item.getTotalAmount() == 0 && item.getPrice() > 0) {
            item.setTotalAmount(item.getPrice() * item.getCount());
        }

        // 주문 시간이 없으면 현재 시간으로 설정
        if (item.getOrderTime() == null) {
            item.setOrderTime(LocalDateTime.now().toString());
        }

        return fnbRepository.save(item);
    }

    public List<FnbItem> getAllItems() {
        return fnbRepository.findAll();
    }

    public boolean deleteItem(String id) {
        return fnbRepository.deleteById(id);
    }
}
package com.team3.repository;

import java.util.List;
import com.team3.model.FnbItem;

public interface FnbRepository {
    boolean save(FnbItem item);
    List<FnbItem> findAll();
    List<FnbItem> findByRoomId(String roomId); // 객실별 내역 조회용
    boolean deleteById(String id);
}
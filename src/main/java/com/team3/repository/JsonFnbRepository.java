package com.team3.repository;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.model.FnbItem;
import com.team3.util.JsonFileManager;

public class JsonFnbRepository implements FnbRepository {
    private static final Logger logger = LoggerFactory.getLogger(JsonFnbRepository.class);
    private final JsonFileManager<FnbItem> fileManager;

    public JsonFnbRepository(JsonFileManager<FnbItem> fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public boolean save(FnbItem item) {
        List<FnbItem> list = fileManager.readAll();
        list.add(item);
        fileManager.writeAll(list);
        logger.info("F&B 저장 완료: {} ({})", item.getMenuName(), item.getPaymentMethod());
        return true;
    }

    @Override
    public List<FnbItem> findAll() {
        return fileManager.readAll();
    }

    @Override
    public List<FnbItem> findByRoomId(String roomId) {
        return fileManager.readAll().stream()
                .filter(item -> roomId.equals(item.getRoomId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        List<FnbItem> list = fileManager.readAll();
        boolean removed = list.removeIf(item -> item.getId().equals(id));
        if (removed) {
            fileManager.writeAll(list);
            return true;
        }
        return false;
    }
}
package com.team3.repository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.Room;
import com.team3.util.JsonFileManager;

public class JsonRoomRepository implements RoomRepository{
    private static final Logger logger = LoggerFactory.getLogger(JsonUserRepository.class);  
    private final JsonFileManager<Room> fileManager;

    public JsonRoomRepository(JsonFileManager<Room> jsonFileManager) {
        logger.info("JsonRoomRepository 초기화 시작...");
        this.fileManager = jsonFileManager;
        logger.info("JsonRoomRepository 초기화 완료");
    }

    @Override
    public boolean add(Room room) {
        logger.info("객실 저장: {}", room.getRoomId());
        List<Room> rooms = fileManager.readAll();


        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId() == room.getRoomId()) {
                logger.debug("기존 객실 존재: {}", room.getRoomId());
                return false;
            }
        }

        rooms.add(room);
        logger.debug("새 객실 추가: {}", room.getRoomId());
        countRooms();
        
        fileManager.writeAll(rooms);
        return true;
    }

    @Override
    public boolean update(Room room) {
        logger.info("객실 업데이트: {}", room.getRoomId());
        
        List<Room> rooms = fileManager.readAll();
        
        // 기존 사용자 업데이트
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId() == room.getRoomId()) {
                rooms.set(i, room);
                logger.debug("기존 객실 업데이트: {}", room.getRoomId());
                fileManager.writeAll(rooms);
                return true;
            }
        }
        logger.debug("기존 객실 존재하지 않음: {}", room.getRoomId());
        return false;
    }

    @Override
    public Optional<Room> findById(int roomId) {
        logger.debug("객실 조회: userId={}", roomId);
        
        return fileManager.readAll().stream()
            .filter(user -> user.getRoomId() == roomId)
            .findFirst();
    }

    @Override
    public List<Room> findAll() {
        logger.debug("전체 객실 조회");
        return fileManager.readAll();
    }

    @Override
    public Optional<Room> deleteById(int roomId) {
        logger.info("객실 삭제 시도: {}", roomId);

        List<Room> rooms = fileManager.readAll();
        
        // 기존 사용자 삭제
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId() == roomId) {
                Optional<Room> room = Optional.of(rooms.remove(i));
                logger.debug("객실 삭제 성공: {}", roomId);
                fileManager.writeAll(rooms);
                return room;
            }
        }
        logger.debug("삭제하려는 객실이 존재하지 않음");
        return Optional.empty();
    }

    @Override
    public long countRooms() {
        long count = fileManager.readAll().stream().count();
        
        logger.debug("객실 수: {}", count);
        return count;
    }
}

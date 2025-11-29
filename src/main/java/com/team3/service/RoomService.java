package com.team3.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.team3.model.PriceChangeLog;
import com.team3.model.Room;
import com.team3.repository.RoomRepository;
import com.team3.util.LocalDateTimeAdapter;

public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()  // 가독성 좋은 JSON 포맷
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // ← 추가!
        .create();

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.roomRepository = roomRepository;
    }

    public List<Room> getRooms() {
        logger.info("모든 객실 조회 시도");
        return roomRepository.findAll();
    }

    public boolean addRoom(Room room) {
        return roomRepository.add(room);
    }

    public Optional<Room> deleteRoom(int roomId) {
        return roomRepository.deleteById(roomId);
    }

    public boolean updateRoom(Room room, String reason) {
        Optional<Room> previousRoom = roomRepository.findById(room.getRoomId());
        if (previousRoom.isEmpty()) {
            return false;
        }
        
        previousRoom.get().addPriceChangeLog(previousRoom.get().getBasePrice(), room.getBasePrice(), reason);
        previousRoom.get().setBasePrice(room.getBasePrice());
        previousRoom.get().setIsAvailable(room.isAvailable());
        previousRoom.get().setMaxOccupancy(room.getMaxOccupancy());

        room = previousRoom.get();
        return roomRepository.update(room);
    }

    public Optional<List<PriceChangeLog>> getPriceChangeLog(int roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(room.get().getPriceChangeLogs());
        }
    }
}

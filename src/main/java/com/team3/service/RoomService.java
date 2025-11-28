package com.team3.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.PriceChangeLog;
import com.team3.model.Room;
import com.team3.repository.RoomRepository;

public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public Optional<List<PriceChangeLog>> getPriceChangeLog(int roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(room.get().getPriceChangeLogs());
        }
    }
}

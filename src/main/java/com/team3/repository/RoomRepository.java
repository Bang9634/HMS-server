package com.team3.repository;

import java.util.List;
import java.util.Optional;

import com.team3.model.Room;

public interface RoomRepository {
    boolean add(Room room);

    boolean update(Room room);

    Optional<Room> findById(int roomId);

    List<Room> findAll();

    Optional<Room> deleteById(int roomId);
    
    long countRooms();
}

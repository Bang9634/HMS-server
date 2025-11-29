package com.team3.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private final int roomId;
    private int basePrice;
    private  boolean isAvailable;
    private  int maxOccupancy;
    private final List<PriceChangeLog> priceChangeLogs = new ArrayList<>();

    public Room(
        int roomId, int basePrice, boolean isAvailable, int maxOccupancy) {
        this.roomId = roomId;
        this.basePrice = basePrice;
        this.isAvailable = isAvailable;
        this.maxOccupancy = maxOccupancy;
        priceChangeLogs.add(new PriceChangeLog(0, basePrice, LocalDateTime.now(), "객실 추가"));
    }
    public Room(Room room) {
        this.roomId = room.roomId;
        this.basePrice = room.basePrice;
        this.isAvailable = room.isAvailable;
        this.maxOccupancy = room.maxOccupancy;
        priceChangeLogs.add(new PriceChangeLog(0, basePrice, LocalDateTime.now(), "객실 추가"));
    }

    public int getRoomId() { return roomId; }
    public int getBasePrice() { return basePrice; }
    public boolean isAvailable() {return isAvailable; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public List<PriceChangeLog> getPriceChangeLogs() { return priceChangeLogs; }

    public void setBasePrice(int basePrice) { this.basePrice = basePrice; }
    public void setIsAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public void addPriceChangeLog(int oldPrice, int newPrice, String reason) {
        priceChangeLogs.add(new PriceChangeLog(oldPrice, newPrice, LocalDateTime.now(), reason));
    }
}

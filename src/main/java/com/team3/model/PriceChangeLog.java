package com.team3.model;

import java.time.LocalDateTime;

public class PriceChangeLog {
    private final LocalDateTime changedAt;
    private final int oldPrice;
    private final int newPrice;
    private final String reason;

    public PriceChangeLog(int oldPrice, int newPrice, LocalDateTime changedAt, String reason) {
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.changedAt = changedAt;
        this.reason = reason;
    }

    public int getOldPrice() { return oldPrice; }
    public int getNewPrice() { return newPrice; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getReason() { return reason; }
}
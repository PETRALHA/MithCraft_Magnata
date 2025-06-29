package com.mithcraft.magnata.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MagnataRecord {
    private final UUID playerUUID;
    private final String playerName;
    private final double balance;
    private final LocalDateTime date;

    public MagnataRecord(UUID playerUUID, String playerName, double balance, LocalDateTime date) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.balance = balance;
        this.date = date;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public double getBalance() { return balance; }
    public LocalDateTime getDate() { return date; }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
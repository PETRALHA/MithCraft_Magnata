package com.mithcraft.magnata.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MagnataRecord {
    private final UUID playerUUID;
    private final String playerName;
    private final double balance;
    private final LocalDateTime date;

    public MagnataRecord(UUID playerUUID, String playerName, double balance, LocalDateTime date) {
        this.playerUUID = playerUUID != null ? playerUUID : new UUID(0, 0);
        this.playerName = playerName != null ? playerName : "Desconhecido";
        this.balance = Math.max(0, balance);
        this.date = date != null ? date : LocalDateTime.now();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f) em %s", 
            playerName, balance, getFormattedDate());
    }
}
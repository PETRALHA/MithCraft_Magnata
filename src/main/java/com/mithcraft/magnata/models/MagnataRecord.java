package com.mithcraft.magnata.models;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("MagnataRecord")
public class MagnataRecord implements ConfigurationSerializable {
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

    // Métodos de serialização
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", playerUUID.toString());
        data.put("name", playerName);
        data.put("balance", balance);
        data.put("date", date.toString());
        return data;
    }

    public static MagnataRecord deserialize(Map<String, Object> data) {
        try {
            UUID uuid = UUID.fromString((String) data.get("uuid"));
            String name = (String) data.get("name");
            double balance = (double) data.get("balance");
            LocalDateTime date = LocalDateTime.parse((String) data.get("date"));
            return new MagnataRecord(uuid, name, balance, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao desserializar MagnataRecord", e);
        }
    }

    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public double getBalance() { return balance; }
    public LocalDateTime getDate() { return date; }

    // Métodos auxiliares
    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f) em %s", playerName, balance, getFormattedDate());
    }
}
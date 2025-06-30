package com.mithcraft.magnata.models;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SerializableAs("MagnataRecord")
public class MagnataRecord implements ConfigurationSerializable {
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UUID playerUUID;
    private final String playerName;
    private final double balance;
    private final LocalDateTime date;

    public MagnataRecord(UUID playerUUID, String playerName, double balance, LocalDateTime date) {
        this.playerUUID = Objects.requireNonNullElse(playerUUID, new UUID(0, 0));
        this.playerName = Objects.requireNonNullElse(playerName, "Desconhecido");
        this.balance = Math.max(0, balance);
        this.date = Objects.requireNonNullElse(date, LocalDateTime.now());
    }

    // Métodos de serialização otimizados
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", playerUUID.toString());
        data.put("name", playerName);
        data.put("balance", balance);
        data.put("date", date.toString());
        data.put("formatted_date", getFormattedDate(DEFAULT_DATE_FORMATTER));
        return data;
    }

    public static MagnataRecord deserialize(@NotNull Map<String, Object> data) {
        try {
            UUID uuid = UUID.fromString(data.get("uuid").toString());
            String name = data.get("name").toString();
            double balance = Double.parseDouble(data.get("balance").toString());
            LocalDateTime date = LocalDateTime.parse(data.get("date").toString());
            
            return new MagnataRecord(uuid, name, balance, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos para MagnataRecord", e);
        }
    }

    // Getters otimizados
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public double getBalance() { return balance; }
    public LocalDateTime getDate() { return date; }

    // Métodos auxiliares aprimorados
    public String getFormattedDate() {
        return getFormattedDate(DEFAULT_DATE_FORMATTER);
    }

    public String getFormattedDate(DateTimeFormatter formatter) {
        return date.format(Objects.requireNonNullElse(formatter, DEFAULT_DATE_FORMATTER));
    }

    // Novo: Formatação de valor monetário
    public String getFormattedBalance() {
        return String.format("%,.2f", balance);
    }

    @Override
    public String toString() {
        return String.format("MagnataRecord[uuid=%s, name=%s, balance=%.2f, date=%s]",
            playerUUID, playerName, balance, getFormattedDate());
    }

    // Implementação equals e hashCode para comparar registros
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagnataRecord that = (MagnataRecord) o;
        return playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }
}

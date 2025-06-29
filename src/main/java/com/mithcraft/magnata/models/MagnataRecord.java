package com.mithcraft.magnata.models;

import org.bukkit.OfflinePlayer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MagnataRecord {
    private final OfflinePlayer player;
    private final double balance;
    private final LocalDateTime date;

    public MagnataRecord(OfflinePlayer player, double balance) {
        this(player, balance, LocalDateTime.now());
    }

    public MagnataRecord(OfflinePlayer player, double balance, LocalDateTime date) {
        this.player = player;
        this.balance = balance;
        this.date = date;
    }

    // Getters
    public OfflinePlayer getPlayer() { return player; }
    public String getPlayerName() { return player.getName(); }
    public double getBalance() { return balance; }
    public LocalDateTime getDate() { return date; }
    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
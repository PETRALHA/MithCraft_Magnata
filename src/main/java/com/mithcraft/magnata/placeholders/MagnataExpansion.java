package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;

public class MagnataExpansion extends PlaceholderExpansion {
    private final MagnataPlugin plugin;

    public MagnataExpansion(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "magnata";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) return "Nenhum";

        switch (params.toLowerCase()) {
            case "name":
                return current.getPlayerName();
            case "balance":
                return String.format("%,.2f", current.getBalance());
            case "balance_raw":
                return String.valueOf(current.getBalance());
            case "date":
                return current.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "time":
                return current.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "datetime":
                return current.getFormattedDate();
            default:
                return null;
        }
    }
}
package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) return "Ninguém";

        switch (params.toLowerCase()) {
            case "name":
                return current.getPlayerName();
            case "balance":
                return String.format("%,.2f", current.getBalance());
            case "date":
                return current.getFormattedDate();
            case "iscurrent":
                return player != null && player.getUniqueId().equals(current.getPlayerUUID()) ? "sim" : "não";
            default:
                return null;
        }
    }
}
package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
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
        if (plugin.getHistoryManager().getCurrentMagnata() == null) {
            return "Nenhum";
        }

        switch (params.toLowerCase()) {
            case "name":
                return plugin.getHistoryManager().getCurrentMagnata().getPlayerName();
            case "balance":
                return String.format("%,.2f", plugin.getHistoryManager().getCurrentMagnata().getBalance());
            case "date":
                return plugin.getHistoryManager().getCurrentMagnata().getFormattedDate();
            default:
                return null;
        }
    }
}
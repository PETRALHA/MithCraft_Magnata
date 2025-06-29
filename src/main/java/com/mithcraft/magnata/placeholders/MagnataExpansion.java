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
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (plugin.getHistoryManager() == null || 
            plugin.getHistoryManager().getCurrentMagnata() == null) {
            return "Nenhum";
        }

        MagnataRecord magnata = plugin.getHistoryManager().getCurrentMagnata();
        
        switch (params.toLowerCase()) {
            case "name":
                return magnata.getPlayerName();
            case "balance":
                return String.format("%,.2f", magnata.getBalance());
            case "date":
                return magnata.getFormattedDate();
            case "all":
                return String.format("%s (%.2f) em %s",
                    magnata.getPlayerName(),
                    magnata.getBalance(),
                    magnata.getFormattedDate());
            default:
                return null;
        }
    }
}
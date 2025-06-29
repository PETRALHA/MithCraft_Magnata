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
        return "SeuNome";
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
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) return "Nenhum";

        switch (params.toLowerCase()) {
            case "current_name":
                return current.getPlayerName();
            case "current_balance":
                return String.format("%.2f", current.getBalance());
            case "current_date":
                return current.getFormattedDate();
            case "is_current":
                return current.getPlayer().getUniqueId().equals(player.getUniqueId()) ? "Sim" : "Não";
            default:
                return null;
        }
    }
}
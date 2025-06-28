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
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("current_name")) {
            OfflinePlayer magnata = plugin.getHistoryManager().getCurrentMagnata();
            return magnata != null ? magnata.getName() : "Nenhum";
        }
        return null;
    }
}
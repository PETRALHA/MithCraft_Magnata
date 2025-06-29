package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class HistoryManager {
    private final MagnataPlugin plugin;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateGroup(OfflinePlayer player) {
        if (plugin.getLuckPerms() != null) {
            // Implementação com LuckPerms
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                "lp user " + player.getName() + " parent add magnata");
        }
    }
}
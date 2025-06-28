package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class RewardManager {
    private final MagnataPlugin plugin;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    public void executeRewards(OfflinePlayer player, boolean onBecome) {
        String path = onBecome ? "rewards.on_become" : "rewards.periodic.commands";
        
        plugin.getConfig().getStringList(path).forEach(reward -> {
            String processed = reward
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        });
    }
}
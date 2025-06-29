package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class RewardManager {
    private final MagnataPlugin plugin;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    public void executeRewards(OfflinePlayer player, boolean onBecome) {
        String path = onBecome ? "rewards.on_become" : "rewards.periodic.commands";
        List<String> rewards = plugin.getConfig().getStringList(path);

        for (String reward : rewards) {
            String processedReward = processPlaceholders(reward, player);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedReward);
            });
        }
    }

    private String processPlaceholders(String command, OfflinePlayer player) {
        return command
            .replace("%player%", player.getName())
            .replace("%uuid%", player.getUniqueId().toString())
            .replace("%balance%", String.format("%.2f", plugin.getEconomy().getBalance(player)));
    }
}
package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class RewardManager {
    private final MagnataPlugin plugin;
    private BukkitTask periodicRewardTask;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer player) {
        executeRewardCommands(plugin.getConfig().getStringList("rewards.on_become"), player);
    }

    public void checkPeriodicRewards() {
        if (plugin.getHistoryManager().getCurrentMagnata() != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(
                plugin.getHistoryManager().getCurrentMagnata().getPlayerUUID()
            );
            givePeriodicRewards(player);
        }
    }

    private void givePeriodicRewards(OfflinePlayer player) {
        executeRewardCommands(plugin.getConfig().getStringList("rewards.periodic.commands"), player);
    }

    private void executeRewardCommands(List<String> commands, OfflinePlayer player) {
        String playerName = player.getName();
        if (playerName == null) return;

        commands.forEach(command -> {
            String formattedCommand = command.replace("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
        });
    }

    private void startPeriodicRewards() {
        int interval = plugin.getConfig().getInt("rewards.periodic.interval_minutes", 60) * 60 * 20;
        if (interval <= 0) return;

        periodicRewardTask = Bukkit.getScheduler().runTaskTimer(plugin, 
            this::checkPeriodicRewards, interval, interval);
    }

    public void reload() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
        startPeriodicRewards();
    }
}
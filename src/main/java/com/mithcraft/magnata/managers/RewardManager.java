package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
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

    public void checkPeriodicRewards() {
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(current.getPlayerUUID());
            givePeriodicRewards(player);
        }
    }

    public void giveBecomeMagnataRewards(OfflinePlayer player) {
        List<String> commands = plugin.getConfig().getStringList("rewards.on_become");
        executeRewardCommands(commands, player);
    }

    public void givePeriodicRewards(OfflinePlayer player) {
        List<String> commands = plugin.getConfig().getStringList("rewards.periodic.commands");
        executeRewardCommands(commands, player);
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

        periodicRewardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkPeriodicRewards();
        }, interval, interval);
    }

    public void reload() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
        startPeriodicRewards();
    }
}
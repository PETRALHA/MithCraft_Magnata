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
        this.periodicRewardTask = startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer player) {
        executeRewardCommands("on_become", player);
    }

    public void givePeriodicRewards(OfflinePlayer player) {
        executeRewardCommands("periodic", player);
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        if (player.getName() == null) return;

        List<String> commands = plugin.getMainConfig().getStringList("rewards." + rewardType + ".commands");
        String playerName = player.getName();

        commands.forEach(command -> {
            try {
                String formatted = command.replace("%player%", playerName);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao executar recompensa: " + e.getMessage());
            }
        });
    }

    private BukkitTask startPeriodicRewards() {
        int interval = plugin.getMainConfig().getInt("rewards.periodic.interval_minutes", 60);
        if (interval <= 0) return null;

        long ticks = interval * 60L * 20L;
        return Bukkit.getScheduler().runTaskTimer(plugin,
            () -> {
                if (plugin.getHistoryManager().getCurrentMagnata() != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(
                        plugin.getHistoryManager().getCurrentMagnata().getPlayerUUID()
                    );
                    givePeriodicRewards(player);
                }
            }, ticks, ticks);
    }

    public void reload() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
        this.periodicRewardTask = startPeriodicRewards();
    }
}
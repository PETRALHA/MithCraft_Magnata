package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Level;

public class RewardManager {
    private final MagnataPlugin plugin;
    private BukkitTask periodicRewardTask;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer player) {
        if (player.getName() == null) return;
        executeRewardCommands("on_become", player);
    }

    public void givePeriodicRewards(OfflinePlayer player) {
        if (player.getName() == null) return;
        executeRewardCommands("periodic", player);
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        List<String> commands = plugin.getMainConfig().getStringList("rewards." + rewardType + ".commands");
        if (commands.isEmpty()) return;

        String playerName = player.getName();
        commands.forEach(command -> {
            try {
                String formatted = command.replace("%player%", playerName);
                if (plugin.getMainConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Executando comando: " + formatted);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erro ao executar recompensa:", e);
            }
        });
    }

    private void startPeriodicRewards() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }

        int interval = plugin.getMainConfig().getInt("rewards.periodic.interval_minutes", 60);
        if (interval <= 0) return;

        long ticks = interval * 60L * 20L;
        periodicRewardTask = Bukkit.getScheduler().runTaskTimer(plugin,
            () -> {
                if (plugin.getHistoryManager().getCurrentMagnata() != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(
                        plugin.getHistoryManager().getCurrentMagnata().getPlayerUUID()
                    );
                    if (player.getName() != null) {
                        givePeriodicRewards(player);
                    }
                }
            }, ticks, ticks);
    }

    public void reload() {
        startPeriodicRewards();
    }
}
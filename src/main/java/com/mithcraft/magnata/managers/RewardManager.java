package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.logging.Level;

public class RewardManager {
    private final MagnataPlugin plugin;
    private BukkitTask periodicRewardTask;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer newMagnata) {
        // 1. Remover do magnata anterior
        removePreviousMagnata();

        // 2. Aplicar recompensas ao novo
        executeRewardCommands("on_become", newMagnata);
    }

    private void removePreviousMagnata() {
        try {
            List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
            if (history.size() > 1) { // Posição 0 é o atual, 1 é o anterior
                String previousMagnata = history.get(1).getPlayerName();
                executeRewardCommand(
                    PlaceholderAPI.setPlaceholders(null, 
                        "lp user %magnata_rank_2% parent remove magnata"
                    )
                );
                plugin.getLogger().info("Removido grupo do magnata anterior: " + previousMagnata);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao remover magnata anterior", e);
        }
    }

    public void givePeriodicRewards(OfflinePlayer player) {
        executeRewardCommands("periodic", player);
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        if (player.getName() == null) return;

        List<String> commands = plugin.getConfig().getStringList("rewards." + rewardType);
        for (String command : commands) {
            executeRewardCommand(command.replace("%player%", player.getName()));
        }
    }

    private void executeRewardCommand(String command) {
        try {
            String formatted = plugin.isPlaceholderApiEnabled() 
                ? PlaceholderAPI.setPlaceholders(null, command)
                : command;

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Executando: " + formatted);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao executar recompensa:", e);
        }
    }

    private BukkitTask startPeriodicRewards() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }

        int interval = plugin.getConfig().getInt("rewards.periodic.interval_minutes", 60);
        if (interval <= 0) return null;

        long ticks = interval * 60L * 20L;
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
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

package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.PlaceholderAPI;
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

    // Novo método adicionado para resolver o erro de compilação
    public void checkMagnata() {
        plugin.getHistoryManager().checkForNewMagnata();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer newMagnata) {
        removePreviousMagnata();
        executeRewardCommands("on_become", newMagnata);
    }

    private void removePreviousMagnata() {
        try {
            List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
            if (history.size() > 1) { // Índice 1 é o magnata anterior
                String command = PlaceholderAPI.setPlaceholders(null, 
                    "lp user %magnata_rank_2% parent remove magnata"
                );
                executeRewardCommand(command);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Falha ao remover magnata anterior", e);
        }
    }

    public void givePeriodicRewards(OfflinePlayer player) {
        executeRewardCommands("periodic", player);
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        if (player.getName() == null) return;

        List<String> commands = plugin.getConfig().getStringList("rewards." + rewardType);
        commands.forEach(command -> 
            executeRewardCommand(command.replace("%player%", player.getName()))
        );
    }

    private void executeRewardCommand(String command) {
        try {
            String formatted = plugin.isPlaceholderApiEnabled() 
                ? PlaceholderAPI.setPlaceholders(null, command)
                : command;

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[Recompensa] Executando: " + formatted);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro na recompensa: " + command, e);
        }
    }

    private BukkitTask startPeriodicRewards() {
        if (periodicRewardTask != null) periodicRewardTask.cancel();

        int interval = plugin.getConfig().getInt("rewards.periodic.interval_minutes", 60);
        if (interval <= 0) return null;

        long ticks = interval * 60L * 20L;
        periodicRewardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getHistoryManager().getCurrentMagnata() != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(
                    plugin.getHistoryManager().getCurrentMagnata().getPlayerUUID()
                );
                givePeriodicRewards(player);
            }
        }, ticks, ticks);

        return periodicRewardTask;
    }

    public void reload() {
        startPeriodicRewards();
    }
}
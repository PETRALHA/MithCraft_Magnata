package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class RewardManager {
    private final MagnataPlugin plugin;
    private BukkitTask periodicRewardTask;
    private final CommandSender console;

    public RewardManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.console = Bukkit.getConsoleSender();
        this.periodicRewardTask = startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer newMagnata) {
        Objects.requireNonNull(newMagnata, "Jogador não pode ser nulo");
        
        // 1. Registrar novo magnata
        executeRewardCommands("on_become", newMagnata);
        
        // 2. Remover o anterior (se existir e for diferente)
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        if (history.size() > 1) {
            executeRewardCommand("lp user %magnata_previous_name% parent remove magnata");
        }
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        List<String> commands = plugin.getConfig().getStringList("rewards." + rewardType);
        if (commands.isEmpty() || player.getName() == null) return;

        commands.forEach(command -> executeRewardCommand(
            command.replace("%player%", player.getName())
                  .replace("%uuid%", player.getUniqueId().toString())
        ));
    }

    private void executeRewardCommand(String command) {
        try {
            String formatted = formatCommand(command);
            logDebug("[Recompensa] Executando: " + formatted);
            Bukkit.getScheduler().callSyncMethod(plugin, () -> 
                Bukkit.dispatchCommand(console, formatted)
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao executar: " + command, e);
        }
    }

    private String formatCommand(String command) {
        return plugin.isPlaceholderApiEnabled() 
            ? PlaceholderAPI.setPlaceholders(null, command)
            : command;
    }

    private BukkitTask startPeriodicRewards() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }

        int interval = plugin.getConfig().getInt("rewards.periodic.interval_minutes", 60) * 60 * 20;
        if (interval <= 0) return null;

        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
            if (current != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(current.getPlayerUUID());
                if (player.hasPlayedBefore()) {
                    executeRewardCommands("periodic", player);
                }
            }
        }, interval, interval);
    }

    private void logDebug(String message) {
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info(message);
        }
    }

    public void reload() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
        this.periodicRewardTask = startPeriodicRewards();
    }

    public void shutdown() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
    }
}

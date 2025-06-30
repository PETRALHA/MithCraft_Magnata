package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
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
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.console = Bukkit.getConsoleSender();
        this.periodicRewardTask = startPeriodicRewards();
    }

    public void giveBecomeMagnataRewards(OfflinePlayer newMagnata) {
        Objects.requireNonNull(newMagnata, "Jogador não pode ser nulo");
        
        // Log detalhado antes de executar
        logMagnataChange(newMagnata);
        
        // 1. Executar recompensas padrão
        executeRewardCommands("on_become", newMagnata);
    }

    private void logMagnataChange(OfflinePlayer newMagnata) {
        MagnataRecord previous = !plugin.getHistoryManager().getHistory().isEmpty() 
            ? plugin.getHistoryManager().getHistory().get(0) 
            : null;

        plugin.getLogger().info("Atualização de Magnata:\n" +
            "Novo: " + newMagnata.getName() + " (UUID: " + newMagnata.getUniqueId() + ")\n" +
            "Anterior: " + (previous != null 
                ? previous.getPlayerName() + " (UUID: " + previous.getPlayerUUID() + ")" 
                : "Nenhum"));
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
            plugin.getLogger().log(Level.WARNING, "Erro ao executar comando: " + command, e);
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

        int intervalTicks = plugin.getConfig().getInt("rewards.periodic.interval_minutes", 60) * 60 * 20;
        if (intervalTicks <= 0) return null;

        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
            if (current != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(current.getPlayerUUID());
                if (player.hasPlayedBefore()) {
                    executeRewardCommands("periodic", player);
                }
            }
        }, intervalTicks, intervalTicks);
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

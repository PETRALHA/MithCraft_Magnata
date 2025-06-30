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
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        MagnataRecord previous = !history.isEmpty() ? history.get(0) : null;

        plugin.getLogger().info("Atualização de Magnata:\n" +
            "Novo: " + newMagnata.getName() + " (UUID: " + newMagnata.getUniqueId() + ")\n" +
            "Anterior: " + (previous != null 
                ? previous.getPlayerName() + " (UUID: " + previous.getPlayerUUID() + ")" 
                : "Nenhum"));
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player) {
        List<String> commands = plugin.getConfig().getStringList("rewards." + rewardType);
        if (commands.isEmpty() || player.getName() == null) {
            logDebug("Nenhum comando de recompensa encontrado para: " + rewardType);
            return;
        }

        commands.forEach(command -> {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());
            executeRewardCommand(processedCommand);
        });
    }

    private void executeRewardCommand(String command) {
        try {
            String formatted = formatCommand(command);
            logDebug("[Recompensa] Executando: " + formatted);
            
            if (Bukkit.isPrimaryThread()) {
                Bukkit.dispatchCommand(console, formatted);
            } else {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> 
                    Bukkit.dispatchCommand(console, formatted)
                );
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao executar comando: " + command, e);
        }
    }

    private String formatCommand(String command) {
        if (!plugin.isPlaceholderApiEnabled()) {
            return command;
        }
        
        try {
            return PlaceholderAPI.setPlaceholders(null, command);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao processar placeholders no comando: " + command, e);
            return command;
        }
    }

    private BukkitTask startPeriodicRewards() {
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }

        int intervalMinutes = plugin.getConfig().getInt("rewards.periodic.interval", 60);
        if (intervalMinutes <= 0) {
            logDebug("Recompensas periódicas desativadas (intervalo <= 0)");
            return null;
        }

        long intervalTicks = intervalMinutes * 60 * 20;
        logDebug("Iniciando recompensas periódicas com intervalo de " + intervalMinutes + " minutos");

        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
            if (current == null) {
                logDebug("Nenhum magnata atual para recompensas periódicas");
                return;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(current.getPlayerUUID());
            if (player.hasPlayedBefore()) {
                logDebug("Distribuindo recompensas periódicas para: " + current.getPlayerName());
                executeRewardCommands("periodic.commands", player);
            } else {
                logDebug("Jogador " + current.getPlayerName() + " nunca jogou, ignorando recompensas");
            }
        }, intervalTicks, intervalTicks);
    }

    private void logDebug(String message) {
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void reload() {
        logDebug("Recarregando RewardManager...");
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
        this.periodicRewardTask = startPeriodicRewards();
    }

    public void shutdown() {
        logDebug("Desativando RewardManager...");
        if (periodicRewardTask != null) {
            periodicRewardTask.cancel();
        }
    }
}
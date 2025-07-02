package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        
        MagnataRecord previousRecord = getPreviousMagnataRecord();
        String previousPlayerName = previousRecord != null ? previousRecord.getPlayerName() : null;
        
        logMagnataChange(newMagnata, previousPlayerName);
        executeRewardCommands("on_become", newMagnata, previousPlayerName);
    }

    private MagnataRecord getPreviousMagnataRecord() {
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        return history.size() > 1 ? history.get(1) : null;
    }

    private void logMagnataChange(OfflinePlayer newMagnata, String previousPlayerName) {
        plugin.getLogger().info("Atualização de Magnata:\n" +
            "Novo: " + newMagnata.getName() + " (UUID: " + newMagnata.getUniqueId() + ")\n" +
            "Anterior: " + (previousPlayerName != null ? previousPlayerName : "Nenhum"));
    }

    private void executeRewardCommands(String rewardType, OfflinePlayer player, String previousPlayer) {
        List<String> commands = plugin.getConfig().getStringList("rewards." + rewardType);
        if (commands.isEmpty() || player.getName() == null) {
            logDebug("Nenhum comando de recompensa encontrado para: " + rewardType);
            return;
        }

        commands.forEach(command -> {
            String processedCommand = replaceAllPlaceholders(command, player, previousPlayer);
            executeRewardCommand(processedCommand);
        });
    }

    private String replaceAllPlaceholders(String command, OfflinePlayer player, String previousPlayer) {
        String result = command
            .replace("%player%", player.getName())
            .replace("%uuid%", player.getUniqueId().toString())
            .replace("%magnata_player%", player.getName())
            .replace("%magnata_previous_player%", previousPlayer != null ? previousPlayer : "")
            .replace("%magnata_balance%", getCurrentBalanceFormatted(player))
            .replace("%magnata_date%", getCurrentDateFormatted());

        if (plugin.isPlaceholderApiEnabled()) {
            try {
                result = PlaceholderAPI.setPlaceholders(player.getPlayer(), result);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erro ao processar placeholders no comando: " + command, e);
            }
        }

        logDebug("[Placeholders] Comando original: " + command);
        logDebug("[Placeholders] Comando processado: " + result);
        return result;
    }

    private String getCurrentBalanceFormatted(OfflinePlayer player) {
        try {
            return plugin.formatCurrency(plugin.getEconomy().getBalance(player));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao obter saldo do jogador", e);
            return "0";
        }
    }

    private String getCurrentDateFormatted() {
        return new SimpleDateFormat(plugin.getMessages().getString("formats.date", "dd/MM/yyyy HH:mm"))
               .format(new Date());
    }

    private void executeRewardCommand(String command) {
        try {
            if (command == null || command.trim().isEmpty()) {
                logDebug("Ignorando comando vazio");
                return;
            }

            logDebug("[Recompensa] Executando: " + command);
            
            if (Bukkit.isPrimaryThread()) {
                Bukkit.dispatchCommand(console, command);
            } else {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> 
                    Bukkit.dispatchCommand(console, command)
                );
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao executar comando: " + command, e);
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
                    executeRewardCommands("periodic", player, null); // CORREÇÃO AQUI
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
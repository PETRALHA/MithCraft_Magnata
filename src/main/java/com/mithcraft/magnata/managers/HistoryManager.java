package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final List<MagnataRecord> history;
    private MagnataRecord currentMagnata;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.history = new ArrayList<>();
    }

    public void checkForNewMagnata() {
        if (plugin.getEconomy() == null) {
            plugin.getLogger().warning("Economia não disponível para verificar magnata");
            return;
        }

        OfflinePlayer richestPlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(OfflinePlayer::hasPlayedBefore)
                .max(Comparator.comparingDouble(p -> plugin.getEconomy().getBalance(p)))
                .orElse(null);

        if (richestPlayer == null) return;

        double balance = plugin.getEconomy().getBalance(richestPlayer);
        
        if (currentMagnata == null || 
            !currentMagnata.getPlayerUUID().equals(richestPlayer.getUniqueId()) ||
            plugin.getMainConfig().getBoolean("settings.force_recheck", false)) {
            setNewMagnata(richestPlayer, balance);
        }
    }

    private void setNewMagnata(OfflinePlayer player, double balance) {
        MagnataRecord newMagnata = new MagnataRecord(
                player.getUniqueId(),
                player.getName(),
                balance,
                LocalDateTime.now()
        );

        if (currentMagnata != null) {
            history.add(0, currentMagnata);
            trimHistory();
        }

        currentMagnata = newMagnata;
        giveRewardsAndNotify(player, balance);
    }

    private void trimHistory() {
        int maxHistory = plugin.getMainConfig().getInt("settings.max_history_size", 10);
        while (history.size() > maxHistory) {
            history.remove(history.size() - 1);
        }
    }

    private void giveRewardsAndNotify(OfflinePlayer player, double balance) {
        plugin.getRewardManager().giveBecomeMagnataRewards(player);
        
        String playerName = player.getName() != null ? player.getName() : "JogadorDesconhecido";
        String balanceFormatted = String.format("%,.2f", balance);
        
        List<String> broadcastMessages = plugin.getMessages().getStringList("broadcast_new_magnata");
        broadcastMessages.forEach(msg -> Bukkit.broadcastMessage(
            msg.replace("%player%", playerName)
              .replace("%balance%", balanceFormatted)
        ));
    }

    public List<MagnataRecord> getHistory() {
        List<MagnataRecord> fullHistory = new ArrayList<>();
        if (currentMagnata != null) {
            fullHistory.add(currentMagnata);
        }
        fullHistory.addAll(history);
        return Collections.unmodifiableList(fullHistory);
    }

    public MagnataRecord getCurrentMagnata() {
        return currentMagnata;
    }

    public void reload() {
        // Implementação futura para carregar de arquivo se necessário
    }
}
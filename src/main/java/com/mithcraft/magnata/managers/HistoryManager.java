package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final List<MagnataRecord> history;
    private MagnataRecord currentMagnata;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.history = new ArrayList<>();
        loadHistory();
        checkForNewMagnata();
    }

    public void checkForNewMagnata() {
        if (plugin.getEconomy() == null) return;

        OfflinePlayer richestPlayer = findRichestPlayer();
        if (richestPlayer == null) return;

        double balance = plugin.getEconomy().getBalance(richestPlayer);
        
        if (currentMagnata == null || !currentMagnata.getPlayerUUID().equals(richestPlayer.getUniqueId())) {
            setNewMagnata(richestPlayer, balance);
        }
    }

    private OfflinePlayer findRichestPlayer() {
        return Bukkit.getOnlinePlayers().stream()
                .max(Comparator.comparingDouble(p -> plugin.getEconomy().getBalance(p)))
                .orElse(null);
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
        }

        currentMagnata = newMagnata;
        
        // Remove registros antigos se exceder o limite
        int maxHistory = plugin.getConfig().getInt("settings.max_history_size", 10);
        while (history.size() > maxHistory) {
            history.remove(history.size() - 1);
        }
        
        saveHistory();
        announceNewMagnata(player, balance);
        plugin.getRewardManager().giveBecomeMagnataRewards(player);
    }

    private void announceNewMagnata(OfflinePlayer player, double balance) {
        List<String> broadcastMessages = plugin.getConfig().getStringList("messages.broadcast_new_magnata");
        broadcastMessages.forEach(msg -> {
            String formatted = msg.replace("%player%", player.getName())
                                .replace("%balance%", String.format("%,.2f", balance));
            Bukkit.broadcastMessage(formatted);
        });
    }

    public List<MagnataRecord> getHistory() {
        List<MagnataRecord> fullHistory = new ArrayList<>();
        if (currentMagnata != null) {
            fullHistory.add(currentMagnata);
        }
        fullHistory.addAll(history);
        return fullHistory;
    }

    public MagnataRecord getCurrentMagnata() {
        return currentMagnata;
    }

    public void reload() {
        loadHistory();
    }

    private void loadHistory() {
        // Implementar carregamento do histórico do arquivo
    }

    private void saveHistory() {
        // Implementar salvamento do histórico no arquivo
    }
}
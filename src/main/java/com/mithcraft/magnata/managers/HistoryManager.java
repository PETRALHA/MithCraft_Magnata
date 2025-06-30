package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final List<MagnataRecord> history;
    private final List<OfflinePlayer> topPlayersCache;
    private MagnataRecord currentMagnata;
    private final File historyFile;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.history = new ArrayList<>();
        this.topPlayersCache = new ArrayList<>();
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        loadHistory();
        updateTopPlayersCache();
    }

    public void checkForNewMagnata() {
        if (plugin.getEconomy() == null) {
            plugin.getLogger().warning("Economia não disponível para verificar magnata");
            return;
        }

        // Atualiza o cache de top players primeiro
        updateTopPlayersCache();

        if (topPlayersCache.isEmpty()) {
            plugin.getLogger().warning("Nenhum jogador encontrado para verificar magnata");
            return;
        }

        // O primeiro da lista é o mais rico
        OfflinePlayer richestPlayer = topPlayersCache.get(0);
        double balance = plugin.getEconomy().getBalance(richestPlayer);
        
        if (shouldUpdateMagnata(richestPlayer, balance)) {
            setNewMagnata(richestPlayer, balance);
        }
    }

    private void updateTopPlayersCache() {
        topPlayersCache.clear();
        topPlayersCache.addAll(
            Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(Objects::nonNull)
                .filter(p -> p.hasPlayedBefore() && p.getName() != null)
                .sorted(Comparator.comparingDouble(p -> -plugin.getEconomy().getBalance(p))) // Ordena do maior para o menor
                .collect(Collectors.toList())
        );
    }

    private boolean shouldUpdateMagnata(OfflinePlayer player, double newBalance) {
        if (currentMagnata == null) return true;
        if (!currentMagnata.getPlayerUUID().equals(player.getUniqueId())) return true;
        return plugin.getConfig().getBoolean("settings.force_recheck", false);
    }

    private void setNewMagnata(OfflinePlayer player, double balance) {
        MagnataRecord newMagnata = new MagnataRecord(
                player.getUniqueId(),
                Objects.requireNonNull(player.getName()),
                balance,
                LocalDateTime.now()
        );

        if (currentMagnata != null) {
            history.add(0, currentMagnata);
            trimHistory();
        }

        currentMagnata = newMagnata;
        saveHistory();
        updateTopPlayersCache();
        giveRewardsAndNotify(player, balance);
    }

    // ... (métodos trimHistory, giveRewardsAndNotify, loadHistory, saveHistory permanecem iguais) ...

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
        history.clear();
        loadHistory();
        updateTopPlayersCache();
    }

    // ===== NOVOS MÉTODOS PARA RANKING DINÂMICO =====
    
    public String getPlayerAtPosition(int position) {
        if (position <= 0 || position > topPlayersCache.size()) {
            return "N/A";
        }
        OfflinePlayer player = topPlayersCache.get(position - 1);
        return player.getName() != null ? player.getName() : "N/A";
    }

    public double getBalanceAtPosition(int position) {
        if (position <= 0 || position > topPlayersCache.size()) {
            return 0.0;
        }
        return plugin.getEconomy().getBalance(topPlayersCache.get(position - 1));
    }

    public String getDateAtPosition(int position) {
        // Implementação depende de como você quer formatar a data
        return "N/A"; // Substituir por lógica real
    }

    public String getFormattedTopLine(int position) {
        if (position <= 0 || position > topPlayersCache.size()) {
            return plugin.formatMessage("ranking.position_empty")
                .replace("%position%", String.valueOf(position));
        }
        
        OfflinePlayer player = topPlayersCache.get(position - 1);
        double balance = plugin.getEconomy().getBalance(player);
        
        return plugin.formatMessage("ranking.top_line_format")
            .replace("%position%", String.valueOf(position))
            .replace("%player%", player.getName() != null ? player.getName() : "N/A")
            .replace("%balance%", plugin.formatCurrency(balance));
    }

    public int getPlayerPosition(UUID playerUUID) {
        for (int i = 0; i < topPlayersCache.size(); i++) {
            if (topPlayersCache.get(i).getUniqueId().equals(playerUUID)) {
                return i + 1;
            }
        }
        return -1; // Não encontrado
    }
}
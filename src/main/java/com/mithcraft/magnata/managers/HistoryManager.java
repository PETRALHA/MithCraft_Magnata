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
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.history = new ArrayList<>();
        this.topPlayersCache = new ArrayList<>();
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        this.loadHistory();
        this.updateTopPlayersCache();
    }

    // =========================================
    // Métodos Principais
    // =========================================

    public void checkForNewMagnata() {
        if (plugin.getEconomy() == null) {
            plugin.getLogger().warning("Economia não disponível para verificar magnata");
            return;
        }

        this.updateTopPlayersCache();

        if (topPlayersCache.isEmpty()) {
            plugin.getLogger().warning("Nenhum jogador encontrado para verificar magnata");
            return;
        }

        OfflinePlayer richestPlayer = topPlayersCache.get(0);
        double balance = plugin.getEconomy().getBalance(richestPlayer);
        
        if (shouldUpdateMagnata(richestPlayer, balance)) {
            setNewMagnata(richestPlayer, balance);
        }
    }

    // =========================================
    // Métodos de Atualização
    // =========================================

    private void updateTopPlayersCache() {
        topPlayersCache.clear();
        topPlayersCache.addAll(
            Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(Objects::nonNull)
                .filter(p -> p.hasPlayedBefore() && p.getName() != null)
                .sorted(Comparator.comparingDouble(p -> -plugin.getEconomy().getBalance(p)))
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
            this.trimHistory();
        }

        currentMagnata = newMagnata;
        this.saveHistory();
        this.updateTopPlayersCache();
        this.giveRewardsAndNotify(player, balance);
    }

    private void trimHistory() {
        int maxHistory = plugin.getConfig().getInt("settings.max_history_size", 10);
        if (history.size() > maxHistory) {
            history.subList(maxHistory, history.size()).clear();
        }
    }

    // =========================================
    // Notificações e Recompensas
    // =========================================

    private void giveRewardsAndNotify(OfflinePlayer player, double balance) {
        plugin.getRewardManager().giveBecomeMagnataRewards(player);
        
        String prefix = plugin.colorize(plugin.getMessages().getString("formats.prefix", ""));
        String playerName = Objects.requireNonNull(player.getName());
        String balanceFormatted = plugin.formatCurrency(balance);
        String previousPlayer = history.isEmpty() ? "Ninguém" : history.get(0).getPlayerName();
        String previousBalance = history.isEmpty() ? plugin.formatCurrency(0) : plugin.formatCurrency(history.get(0).getBalance());

        for (String line : plugin.getMessages().getStringList("notifications.new_magnata")) {
            String message = plugin.colorize(line
                .replace("{prefix}", prefix)
                .replace("{player}", playerName)
                .replace("{balance}", balanceFormatted)
                .replace("{previous_player}", previousPlayer)
                .replace("{previous_balance}", previousBalance));
            
            Bukkit.broadcastMessage(message);
        }
    }

    // =========================================
    // Persistência de Dados (Save/Load)
    // =========================================

    private void loadHistory() {
        if (!historyFile.exists()) return;

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
            
            if (config.isConfigurationSection("current")) {
                ConfigurationSection section = config.getConfigurationSection("current");
                if (section != null) {
                    currentMagnata = MagnataRecord.deserialize(section.getValues(false));
                }
            }
            
            if (config.isList("history")) {
                config.getList("history", Collections.emptyList()).stream()
                    .filter(entry -> entry instanceof Map)
                    .map(entry -> (Map<?, ?>) entry)
                    .map(rawMap -> {
                        Map<String, Object> safeMap = new LinkedHashMap<>();
                        rawMap.forEach((key, value) -> safeMap.put(String.valueOf(key), value));
                        return safeMap;
                    })
                    .forEach(map -> {
                        try {
                            history.add(MagnataRecord.deserialize(map));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Entrada inválida no histórico: " + e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao carregar histórico", e);
        }
    }

    private void saveHistory() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            
            if (currentMagnata != null) {
                config.createSection("current", currentMagnata.serialize());
            }
            
            List<Map<String, Object>> historyData = history.stream()
                .map(MagnataRecord::serialize)
                .collect(Collectors.toList());
            
            config.set("history", historyData);
            config.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao salvar histórico", e);
        }
    }

    // =========================================
    // Métodos Públicos para Consulta
    // =========================================

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
        this.loadHistory();
        this.updateTopPlayersCache();
    }

    // =========================================
    // Métodos de Ranking (PlaceholderAPI)
    // =========================================

    public String getPlayerAtPosition(int position) {
        if (position <= 0 || position > topPlayersCache.size()) return "N/A";
        OfflinePlayer player = topPlayersCache.get(position - 1);
        return player.getName() != null ? player.getName() : "N/A";
    }

    public double getBalanceAtPosition(int position) {
        if (position <= 0 || position > topPlayersCache.size()) return 0.0;
        return plugin.getEconomy().getBalance(topPlayersCache.get(position - 1));
    }

    public int getPlayerPosition(UUID playerUUID) {
        for (int i = 0; i < topPlayersCache.size(); i++) {
            if (topPlayersCache.get(i).getUniqueId().equals(playerUUID)) {
                return i + 1;
            }
        }
        return -1;
    }

    public String getDateAtPosition(int position) {
        if (position <= 0) return "N/A";
        
        // Posição 1 é o magnata atual
        if (position == 1 && currentMagnata != null) {
            return currentMagnata.getFormattedDate();
        }
        
        // Posições > 1 vêm do histórico
        int historyIndex = position - 2;
        if (historyIndex >= 0 && historyIndex < history.size()) {
            return history.get(historyIndex).getFormattedDate();
        }
        
        return "N/A";
    }

    public String getFormattedTopLine(int position) {
        if (position <= 0 || position > topPlayersCache.size()) {
            return plugin.getMessages().getString("ranking.position_empty")
                .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
                .replace("{position}", String.valueOf(position));
        }
        
        OfflinePlayer player = topPlayersCache.get(position - 1);
        double balance = plugin.getEconomy().getBalance(player);
        
        return plugin.getMessages().getString("ranking.top_line_format")
            .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
            .replace("{position}", String.valueOf(position))
            .replace("{player}", player.getName() != null ? player.getName() : "N/A")
            .replace("{balance}", plugin.formatCurrency(balance));
    }
}
package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final List<MagnataRecord> history;
    private MagnataRecord currentMagnata;
    private final File historyFile;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.history = new ArrayList<>();
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        loadHistory();
    }

    public void checkForNewMagnata() {
        if (plugin.getEconomy() == null) {
            plugin.getLogger().warning("Economia não disponível para verificar magnata");
            return;
        }

        Optional<OfflinePlayer> richestPlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.hasPlayedBefore() && p.getName() != null)
                .max(Comparator.comparingDouble(p -> plugin.getEconomy().getBalance(p)));

        if (!richestPlayer.isPresent()) {
            plugin.getLogger().warning("Nenhum jogador encontrado para verificar magnata");
            return;
        }

        double balance = plugin.getEconomy().getBalance(richestPlayer.get());
        
        if (shouldUpdateMagnata(richestPlayer.get(), balance)) {
            setNewMagnata(richestPlayer.get(), balance);
        }
    }

    private boolean shouldUpdateMagnata(OfflinePlayer player, double newBalance) {
        if (currentMagnata == null) return true;
        if (!currentMagnata.getPlayerUUID().equals(player.getUniqueId())) return true;
        return plugin.getMainConfig().getBoolean("settings.force_recheck", false);
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
        saveHistory();
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
        
        String playerName = player.getName();
        String balanceFormatted = plugin.formatCurrency(balance);
        
        plugin.getMessages().getStringList("broadcast_new_magnata").forEach(msg -> 
            Bukkit.broadcastMessage(plugin.formatMessage(msg)
                .replace("%player%", playerName)
                .replace("%balance%", balanceFormatted))
        );
    }

    private void loadHistory() {
        if (!historyFile.exists()) return;

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
            if (config.contains("current")) {
                currentMagnata = MagnataRecord.deserialize(config.getConfigurationSection("current"));
            }
            
            if (config.contains("history")) {
                config.getMapList("history").forEach(map -> 
                    history.add(MagnataRecord.deserialize(map))
                );
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao carregar histórico", e);
        }
    }

    private void saveHistory() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            
            if (currentMagnata != null) {
                config.set("current", currentMagnata.serialize());
            }
            
            List<Map<String, Object>> historyData = new ArrayList<>();
            history.forEach(record -> historyData.add(record.serialize()));
            config.set("history", historyData);
            
            config.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao salvar histórico", e);
        }
    }

    public List<MagnataRecord> getHistory() {
        return Collections.unmodifiableList(new ArrayList<MagnataRecord>() {{
            if (currentMagnata != null) add(currentMagnata);
            addAll(history);
        }});
    }

    public MagnataRecord getCurrentMagnata() {
        return currentMagnata;
    }

    public void reload() {
        history.clear();
        loadHistory();
    }
}
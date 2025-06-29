package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final File historyFile;
    private final List<MagnataRecord> history;
    private final int maxHistorySize;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        this.maxHistorySize = plugin.getConfig().getInt("settings.max_history_size", 10);
        this.history = new ArrayList<>();
        loadHistory();
    }

    public void addRecord(OfflinePlayer player, double balance) {
        MagnataRecord newRecord = new MagnataRecord(player, balance);
        history.add(0, newRecord);
        
        // Manter apenas o histórico mais recente
        if (history.size() > maxHistorySize) {
            history.remove(history.size() - 1);
        }
        
        saveHistory();
    }

    public MagnataRecord getCurrentMagnata() {
        return history.isEmpty() ? null : history.get(0);
    }

    public List<MagnataRecord> getHistory() {
        return new ArrayList<>(history);
    }

    private void loadHistory() {
        if (!historyFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
        List<Map<?, ?>> records = config.getMapList("magnatas");
        
        for (Map<?, ?> entry : records) {
            try {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString((String) entry.get("uuid")));
                double balance = (double) entry.get("balance");
                LocalDateTime date = LocalDateTime.parse((String) entry.get("date"));
                
                history.add(new MagnataRecord(player, balance, date));
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar entrada do histórico: " + e.getMessage());
            }
        }
    }

    private void saveHistory() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            List<Map<String, Object>> data = new ArrayList<>();

            for (MagnataRecord record : history) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("player", record.getPlayerName());
                entry.put("uuid", record.getPlayer().getUniqueId().toString());
                entry.put("balance", record.getBalance());
                entry.put("date", record.getDate().toString());
                data.add(entry);
            }

            config.set("magnatas", data);
            config.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar histórico: " + e.getMessage());
        }
    }
}
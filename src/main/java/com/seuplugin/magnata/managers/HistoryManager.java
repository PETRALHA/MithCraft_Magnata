package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryManager {
    private final MagnataPlugin plugin;
    private final File historyFile;
    private YamlConfiguration historyConfig;

    public HistoryManager(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        reloadHistory();
    }

    public void addRecord(OfflinePlayer player, double balance) {
        List<Map<?, ?>> records = historyConfig.getMapList("magnatas");
        
        Map<String, Object> newRecord = new LinkedHashMap<>();
        newRecord.put("player", player.getName());
        newRecord.put("uuid", player.getUniqueId().toString());
        newRecord.put("balance", balance);
        newRecord.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        records.add(0, newRecord);
        historyConfig.set("magnatas", records);
        saveHistory();
    }

    public List<Map<?, ?>> getHistory() {
        return historyConfig.getMapList("magnatas");
    }

    private void reloadHistory() {
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }

    private void saveHistory() {
        try {
            historyConfig.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar history.yml: " + e.getMessage());
        }
    }
}
package com.mithcraft.magnata.managers;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
                .filter(Objects::nonNull)
                .filter(p -> p.hasPlayedBefore() && p.getName() != null)
                .max(Comparator.comparingDouble(p -> plugin.getEconomy().getBalance(p)));

        if (richestPlayer.isEmpty()) {
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
        giveRewardsAndNotify(player, balance);
    }

    private void trimHistory() {
        int maxHistory = plugin.getConfig().getInt("settings.max_history_size", 10);
        if (history.size() > maxHistory) {
            history.subList(maxHistory, history.size()).clear();
        }
    }

    private void giveRewardsAndNotify(OfflinePlayer player, double balance) {
        plugin.getRewardManager().giveBecomeMagnataRewards(player);
        
        String playerName = Objects.requireNonNull(player.getName());
        String balanceFormatted = plugin.formatCurrency(balance);
        
        plugin.getMessages().getStringList("broadcast_new_magnata").forEach(msg -> {
            Component message = LegacyComponentSerializer.legacySection().deserialize(
                plugin.formatMessage(msg)
                    .replace("%player%", playerName)
                    .replace("%balance%", balanceFormatted)
            );
            Bukkit.getServer().sendMessage(message);
        });
    }

    private void loadHistory() {
        if (!historyFile.exists()) return;

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
            
            // Desserialização segura do magnata atual
            if (config.isConfigurationSection("current")) {
                ConfigurationSection section = config.getConfigurationSection("current");
                if (section != null) {
                    currentMagnata = MagnataRecord.deserialize(section.getValues(false));
                }
            }
            
            // Desserialização segura do histórico
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
    }
}

package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import com.mithcraft.magnata.placeholders.MagnataExpansion;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class MagnataPlugin extends JavaPlugin {
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private Economy economy;
    private LuckPerms luckPerms;
    private FileConfiguration messages;
    
    @Override
    public void onEnable() {
        // Carrega configurações
        saveDefaultConfig();
        createMessagesFile();
        
        // Configura economia (Vault)
        setupEconomy();
        
        // Configura LuckPerms
        setupLuckPerms();
        
        // Inicializa managers
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
        
        // Registra comandos
        registerCommands();
        
        // Registra PlaceholderAPI
        if (getConfig().getBoolean("settings.use_placeholderapi") && 
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
        }
        
        // Agenda verificações periódicas
        scheduleMagnataCheck();
        
        getLogger().info("Plugin Magnata habilitado com sucesso!");
    }

    private void createMessagesFile() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault não encontrado! Economia desativada.");
            return;
        }
        this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
    }

    private void setupLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().warning("LuckPerms não encontrado!");
            return;
        }
        this.luckPerms = getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
    }

    private void registerCommands() {
        MagnataCommand magnataCommand = new MagnataCommand(this);
        Objects.requireNonNull(getCommand("magnata")).setExecutor(magnataCommand);
        Objects.requireNonNull(getCommand("magnata")).setTabCompleter(magnataCommand);
    }

    private void scheduleMagnataCheck() {
        int interval = getConfig().getInt("settings.check_interval_seconds", 300) * 20;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            historyManager.checkForNewMagnata();
            rewardManager.checkPeriodicRewards();
        }, interval, interval);
    }

    public void reloadPlugin() {
        reloadConfig();
        createMessagesFile();
        historyManager.reload();
        rewardManager.reload();
    }

    // Getters
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
}
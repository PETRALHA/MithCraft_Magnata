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
        if (!setupEconomy()) {
            getLogger().severe("Desativando plugin - Vault não encontrado!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Configura LuckPerms
        setupLuckPerms();
        
        // Inicializa managers
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
        
        // Registra comandos
        registerCommands();
        
        // Registra PlaceholderAPI se estiver presente
        if (getConfig().getBoolean("settings.use_placeholderapi", true) && 
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            getLogger().info("PlaceholderAPI registrado com sucesso!");
        }
        
        // Agenda verificações periódicas
        scheduleMagnataCheck();
        
        getLogger().info("Plugin Magnata habilitado com sucesso! Versão " + getDescription().getVersion());
    }

    private void createMessagesFile() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void setupLuckPerms() {
        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
        }
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
        getLogger().info("Configurações recarregadas com sucesso!");
    }

    // Getters
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
}
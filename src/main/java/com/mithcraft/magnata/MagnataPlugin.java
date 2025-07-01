package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import com.mithcraft.magnata.placeholders.MagnataExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public final class MagnataPlugin extends JavaPlugin {
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private Economy economy;
    private boolean placeholderApiEnabled = false;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        // 1. Load configurations
        if (!loadConfigurations()) {
            shutdown("Failed to load configurations");
            return;
        }

        // 2. Setup required dependencies
        if (!setupEconomy()) {
            shutdown("Vault economy not found");
            return;
        }

        // 3. Initialize components
        initializeManagers();
        registerCommands();
        setupOptionalIntegrations();

        // 4. Start background tasks
        startMagnataChecker();

        getLogger().info("Plugin activated successfully!");
    }

    @Override
    public void onDisable() {
        if (rewardManager != null) {
            rewardManager.shutdown();
        }
        getLogger().info("Plugin disabled");
    }

    private boolean loadConfigurations() {
        try {
            // Main config
            saveDefaultConfig();
            reloadConfig();

            // Messages
            File messagesFile = new File(getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                try (InputStream in = getResource("messages.yml")) {
                    if (in != null) {
                        Files.copy(in, messagesFile.toPath());
                    } else {
                        saveResource("messages.yml", false);
                    }
                }
            }
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            return true;
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Configuration loading error:", e);
            return false;
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        getLogger().info("Economy connected: " + economy.getName());
        return true;
    }

    private void initializeManagers() {
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
    }

    private void registerCommands() {
        getCommand("magnata").setExecutor(new MagnataCommand(this));
    }

    private void setupOptionalIntegrations() {
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI integration enabled");
        }
    }

    private void startMagnataChecker() {
        int interval = getConfig().getInt("settings.check_interval", 300) * 20;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (economy != null) {
                historyManager.checkForNewMagnata();
            }
        }, interval, interval);
    }

    private void shutdown(String reason) {
        getLogger().severe(reason);
        getServer().getPluginManager().disablePlugin(this);
    }

    // ===== Utility Methods =====
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return colorize(messages.getString("formats.prefix", ""));
    }

    public String formatCurrency(double amount) {
        return economy != null ? economy.format(amount) : String.format("$%,.2f", amount);
    }

    // ===== Getters =====
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public FileConfiguration getMessages() { return messages; }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import com.mithcraft.magnata.placeholders.MagnataExpansion;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public final class MagnataPlugin extends JavaPlugin {
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private Economy economy;
    private LuckPerms luckPerms;
    private FileConfiguration messages;
    private boolean placeholderApiEnabled = false;

    @Override
    public void onEnable() {
        // Garante que a pasta do plugin existe
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            if (!loadConfigurations()) {
                shutdown("Falha ao carregar configurações");
                return;
            }

            if (!setupEconomy()) {
                shutdown("Economia (Vault) não encontrada");
                return;
            }

            setupLuckPerms();
            setupPlaceholderAPI();
            initializeManagers();
            registerCommands();
            startMagnataChecker();

            getLogger().info("Plugin ativado com sucesso!");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro crítico na inicialização:", e);
            shutdown("Erro na inicialização");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Desativando plugin...");
        if (placeholderApiEnabled) {
            new MagnataExpansion(this).unregister();
        }
        if (rewardManager != null) {
            rewardManager.shutdown();
        }
    }

    public boolean loadConfigurations() {
        try {
            // Configuração principal
            saveDefaultConfig();
            reloadConfig();

            // Arquivo de mensagens
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
            getLogger().log(Level.SEVERE, "Erro ao carregar configurações:", e);
            return false;
        }
    }

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        getLogger().info("Economia conectada: " + economy.getName());
        return true;
    }

    public void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms conectado com sucesso");
        }
    }

    public void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI registrado com sucesso");
        }
    }

    private void initializeManagers() {
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("magnata")).setExecutor(new MagnataCommand(this));
        Objects.requireNonNull(getCommand("mg")).setExecutor(new MagnataCommand(this));
    }

    private void startMagnataChecker() {
        int interval = getConfig().getInt("settings.check_interval_seconds", 300) * 20;
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

    // ===== SISTEMA DE MENSAGENS =====
    public String formatMessage(String path) {
        String message = messages.getString(path, "&cMensagem não encontrada: " + path);
        String prefix = messages.getString("formats.prefix", "");
        
        // Substitui {prefix} apenas se existir na mensagem
        if (message.contains("{prefix}")) {
            message = message.replace("{prefix}", prefix);
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String formatMessage(String path, Map<String, String> replacements) {
        String message = formatMessage(path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    public String formatCurrency(double amount) {
        return economy != null ? economy.format(amount) : String.format("%,.2f", amount);
    }

    // ===== GETTERS =====
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
    public FileConfiguration getMainConfig() { return getConfig(); }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
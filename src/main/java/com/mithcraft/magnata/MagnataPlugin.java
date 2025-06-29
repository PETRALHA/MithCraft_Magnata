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
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
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
        try {
            // 1. Carregar configurações
            if (!loadConfigurations()) {
                shutdown("Falha ao carregar configurações");
                return;
            }

            // 2. Verificar dependências
            if (!setupEconomy()) {
                shutdown("Economia (Vault) não encontrada");
                return;
            }
            setupLuckPerms();
            setupPlaceholderAPI();

            // 3. Inicializar componentes
            initializeManagers();
            registerCommands();
            startMagnataChecker();

            getLogger().info(formatMessage("&aPlugin ativado com sucesso!"));
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro crítico:", e);
            shutdown("Erro na inicialização");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Desativando plugin...");
        if (placeholderApiEnabled) {
            new MagnataExpansion(this).unregister();
        }
    }

    public boolean loadConfigurations() {
        try {
            // Configuração principal
            saveDefaultConfig();
            
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
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao carregar configs:", e);
            return false;
        }
    }

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault não encontrado!");
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Nenhum plugin de economia encontrado!");
            return false;
        }
        
        economy = rsp.getProvider();
        getLogger().info("Economia conectada: " + economy.getName());
        return true;
    }

    public boolean setupLuckPerms() {
        try {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                getLogger().info("LuckPerms conectado com sucesso");
                return true;
            }
            return false;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "LuckPerms não encontrado", e);
            return false;
        }
    }

    public void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI conectado com sucesso");
        }
    }

    private void initializeManagers() {
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
    }

    private void registerCommands() {
        try {
            MagnataCommand cmd = new MagnataCommand(this);
            getCommand("magnata").setExecutor(cmd);
            getCommand("magnata").setTabCompleter(cmd);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao registrar comandos:", e);
        }
    }

    private void startMagnataChecker() {
        int interval = getConfig().getInt("settings.check_interval_seconds", 300) * 20;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            rewardManager.checkMagnata();
        }, interval, interval);
    }

    public String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', 
            getMessages().getString("prefix", "&6[Magnata] &7") + message);
    }

    public String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }

    public void reload() {
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        if (historyManager != null) historyManager.reload();
        if (rewardManager != null) rewardManager.reload();
        setupEconomy();
        setupPlaceholderAPI();
    }

    private void shutdown(String reason) {
        getLogger().severe("Desativando: " + reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    // Getters
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
    public FileConfiguration getMainConfig() { return getConfig(); }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
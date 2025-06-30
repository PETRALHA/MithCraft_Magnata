package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import com.mithcraft.magnata.placeholders.MagnataExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;

public final class MagnataPlugin extends JavaPlugin {
    // Managers
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private Economy economy;
    private LuckPerms luckPerms;
    private FileConfiguration messages;
    private boolean placeholderApiEnabled = false;
    
    // Sistemas de formatação
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    @Override
    public void onEnable() {
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
            registerCommands(); // Método corrigido
            startMagnataChecker();

            getComponentLogger().info(formatComponent("<gradient:gold:white>[MithCraftMagnata]</gradient> <green>Plugin ativado com sucesso!"));

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro crítico na inicialização:", e);
            shutdown("Erro na inicialização");
        }
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(formatComponent("<gold>[MithCraftMagnata]</gold> <gray>Desativando plugin..."));
        if (placeholderApiEnabled) {
            new MagnataExpansion(this).unregister();
        }
        if (rewardManager != null) {
            rewardManager.shutdown();
        }
    }

    // ----- Métodos de Inicialização -----
    public boolean loadConfigurations() {
        try {
            saveDefaultConfig();
            File messagesFile = new File(getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                try (InputStream in = getResource("messages.yml")) {
                    if (in != null) Files.copy(in, messagesFile.toPath());
                    else saveResource("messages.yml", false);
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

    public void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI registrado com sucesso");
        }
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms conectado com sucesso");
        }
    }

    private void initializeManagers() {
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
    }

    // ----- Método Corrigido para Registrar Comandos -----
    private void registerCommands() {
        // Registrar o comando principal e definir seu executor
        Objects.requireNonNull(getCommand("magnata")).setExecutor(new MagnataCommand(this));
        
        // Registrar aliases se necessário
        getCommand("mg").setExecutor(new MagnataCommand(this));
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

    // ----- Sistemas de Formatação -----
    public String formatMessage(String path) {
        String message = messages.getString(path, path);
        return legacySerializer.serialize(
            miniMessage.deserialize(
                messages.getString("prefix", "<gold>[Magnata]</gold> ") + message
            )
        ).replace('§', '&');
    }

    public Component formatComponent(String message) {
        return miniMessage.deserialize(
            messages.getString("prefix", "<gold>[Magnata]</gold> ") + message
        );
    }

    public String formatCurrency(double amount) {
        return economy != null ? economy.format(amount) : String.format("%,.2f", amount);
    }

    // ----- Getters -----
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
    public FileConfiguration getMainConfig() { return getConfig(); }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
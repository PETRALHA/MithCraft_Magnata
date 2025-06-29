package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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

    @Override
    public void onEnable() {
        try {
            // 1. Carregar configurações
            if (!loadConfigurations()) {
                shutdown("Falha ao carregar configurações");
                return;
            }

            // 2. Verificar dependências
            if (!setupEconomy() || !setupLuckPerms()) {
                shutdown("Dependências não encontradas");
                return;
            }

            // 3. Inicializar componentes
            initializeManagers();
            registerCommands();

            getLogger().info("§aPlugin ativado com sucesso!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro crítico:", e);
            shutdown("Erro na inicialização");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Desativando plugin...");
    }

    private boolean loadConfigurations() {
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
                        getLogger().warning("messages.yml padrão não encontrado, criando novo...");
                        messagesFile.createNewFile();
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

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().severe("Vault/Economy não encontrado!");
                return false;
            }
            economy = rsp.getProvider();
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro na economia:", e);
            return false;
        }
    }

    private boolean setupLuckPerms() {
        try {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
            }
            return true;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "LuckPerms não encontrado", e);
            return true; // Não é essencial
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

    public void reload() {
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        if (historyManager != null) historyManager.reload();
        if (rewardManager != null) rewardManager.reload();
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
}
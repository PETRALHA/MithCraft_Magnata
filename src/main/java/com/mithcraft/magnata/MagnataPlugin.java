package com.mithcraft.magnata;

import com.mithcraft.magnata.commands.*;
import com.mithcraft.magnata.managers.*;
import com.mithcraft.magnata.placeholders.MagnataExpansion;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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
            logStartup();
            
            // 1. Carregar configurações
            if (!loadConfigurations()) {
                disablePlugin("Falha ao carregar configurações");
                return;
            }

            // 2. Verificar dependências essenciais
            if (!setupEconomy() || !setupLuckPerms()) {
                disablePlugin("Dependências essenciais não encontradas");
                return;
            }

            // 3. Inicializar componentes
            initializeManagers();
            registerCommands();
            
            // 4. Integrações opcionais
            setupOptionalIntegrations();
            
            // 5. Agendar tarefas
            scheduleTasks();
            
            getLogger().log(Level.INFO, "§aPlugin ativado com sucesso!");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cErro crítico durante a inicialização:", e);
            disablePlugin("Erro crítico");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Desativando MithCraft Magnata...");
        // Limpeza de recursos se necessário
    }

    private void logStartup() {
        getLogger().info("§6=== MithCraft Magnata v" + getDescription().getVersion() + " ===");
        getLogger().info("§6Carregando plugin...");
    }

    private boolean loadConfigurations() {
        try {
            // Configuração principal
            saveDefaultConfig();
            
            // Arquivo de mensagens
            File messagesFile = new File(getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                saveResource("messages.yml", false);
                getLogger().info("Arquivo messages.yml criado");
            }
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao carregar configurações:", e);
            return false;
        }
    }

    private boolean setupEconomy() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                getLogger().severe("Vault não encontrado!");
                return false;
            }

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().severe("Nenhum provedor de economia encontrado!");
                return false;
            }

            economy = rsp.getProvider();
            if (economy == null) {
                getLogger().severe("Falha ao obter provedor de economia");
                return false;
            }

            getLogger().info("Economia conectada: " + economy.getName());
            return true;

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao configurar economia:", e);
            return false;
        }
    }

    private boolean setupLuckPerms() {
        try {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                getLogger().info("LuckPerms conectado com sucesso");
                return true;
            }
            getLogger().warning("LuckPerms não encontrado (algumas features serão desativadas)");
            return true; // Não é essencial

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao configurar LuckPerms:", e);
            return false;
        }
    }

    private void initializeManagers() {
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);
        getLogger().info("Managers inicializados");
    }

    private void registerCommands() {
        try {
            MagnataCommand magnataCommand = new MagnataCommand(this);
            getCommand("magnata").setExecutor(magnataCommand);
            getCommand("magnata").setTabCompleter(magnataCommand);
            getLogger().info("Comandos registrados");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao registrar comandos:", e);
        }
    }

    private void setupOptionalIntegrations() {
        // PlaceholderAPI
        if (getConfig().getBoolean("settings.use_placeholderapi", true)) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                try {
                    new MagnataExpansion(this).register();
                    getLogger().info("PlaceholderAPI integrado");
                } catch (Exception e) {
                    getLogger().warning("Não foi possível registrar PlaceholderAPI: " + e.getMessage());
                }
            }
        }
    }

    private void scheduleTasks() {
        try {
            int interval = getConfig().getInt("settings.check_interval_seconds", 300) * 20;
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                historyManager.checkForNewMagnata();
                rewardManager.checkPeriodicRewards();
            }, interval, interval);
            getLogger().info("Tarefas agendadas (intervalo: " + interval/20 + "s)");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao agendar tarefas:", e);
        }
    }

    public void reloadPlugin() {
        try {
            reloadConfig();
            messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
            historyManager.reload();
            rewardManager.reload();
            getLogger().info("Configurações recarregadas");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro ao recarregar configurações:", e);
        }
    }

    private void disablePlugin(String reason) {
        getLogger().severe("Desativando plugin - Motivo: " + reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    // Getters
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FileConfiguration getMessages() { return messages; }
}
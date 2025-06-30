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
            initializePlugin();
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private void initializePlugin() throws Exception {
        // 1. Carregar configurações
        if (!loadConfigurations()) {
            throw new IllegalStateException("Falha ao carregar configurações");
        }

        // 2. Verificar dependências críticas
        verifyCriticalDependencies();

        // 3. Inicializar componentes
        initializeComponents();

        getComponentLogger().info(formatComponent(
            "<gradient:gold:white>[MithCraftMagnata]</gradient> <green>Ativo! " +
            "<gray>(Economia: " + economy.getName() + ")"
        ));
    }

    protected boolean loadConfigurations() {
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
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Falha crítica ao carregar configurações:", e);
            return false;
        }
    }

    private void verifyCriticalDependencies() {
        if (!setupEconomy()) {
            throw new IllegalStateException("Dependência econômica não disponível");
        }
    }

    protected boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Nenhum plugin de economia encontrado via Vault");
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    private void initializeComponents() {
        // Inicializar managers
        this.historyManager = new HistoryManager(this);
        this.rewardManager = new RewardManager(this);

        // Configurar integrações
        setupLuckPerms();
        setupPlaceholderAPI();

        // Registrar comandos
        registerCommands();
        
        // Iniciar tarefas agendadas
        startScheduledTasks();
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("Integração com LuckPerms estabelecida");
        }
    }

    protected void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MagnataExpansion(this).register();
            placeholderApiEnabled = true;
            getLogger().info("PlaceholderAPI registrado com sucesso");
        }
    }

    private void registerCommands() {
        new MagnataCommand(this);
        new MagnataHelpCommand(this);
        new MagnataHistoryCommand(this);
        new MagnataReloadCommand(this);
    }

    private void startScheduledTasks() {
        // Verificação periódica do magnata
        int interval = getConfig().getInt("settings.check_interval_seconds", 300) * 20;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (economy != null) {
                historyManager.checkForNewMagnata();
            }
        }, interval, interval);
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(formatComponent(
            "<gold>[MithCraftMagnata]</gold> <gray>Desativando..."
        ));
        
        if (placeholderApiEnabled) {
            new MagnataExpansion(this).unregister();
        }
        
        // Limpar recursos
        if (rewardManager != null) {
            rewardManager.shutdown();
        }
    }

    // ----- Sistemas de Formatação -----
    public String formatMessage(String message) {
        return legacySerializer.serialize(
            miniMessage.deserialize(
                getMessages().getString("prefix", "<gold>[Magnata]</gold> <gray>") + message
            )
        ).replace('§', '&');
    }

    public Component formatComponent(String message) {
        return miniMessage.deserialize(
            getMessages().getString("prefix", "<gold>[Magnata]</gold> <gray>") + message
        );
    }

    public String formatCurrency(double amount) {
        return economy != null ? economy.format(amount) : String.format("%,.2f", amount);
    }

    // ----- Tratamento de Erros -----
    private void handleStartupError(Exception e) {
        getLogger().log(Level.SEVERE, """
            ===========================================
            ERRO CRÍTICO NA INICIALIZAÇÃO DO MAGNATA
            Motivo: %s
            ===========================================
            """.formatted(e.getMessage()), e);
        
        shutdown();
    }

    private void shutdown() {
        getServer().getPluginManager().disablePlugin(this);
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

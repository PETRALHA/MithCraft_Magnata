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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public final class MagnataPlugin extends JavaPlugin {
    // Managers
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private Economy economy;
    private LuckPerms luckPerms;
    private FileConfiguration messages;
    private boolean placeholderApiEnabled = false;
    
    // Sistemas de formatação (ambos disponíveis)
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

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

            // Log com ambos sistemas (exemplo)
            getLogger().info(formatMessageLegacy("&aPlugin ativado com sucesso!"));
            getComponentLogger().info(formatComponent("<gradient:gold:white>[MithCraftMagnata]</gradient> <green>Pronto!"));
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Erro crítico:", e);
            shutdown("Erro na inicialização");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(formatMessageLegacy("&6[MithCraftMagnata] &7Desativando plugin..."));
        if (placeholderApiEnabled) {
            new MagnataExpansion(this).unregister();
        }
    }

    // ----- Sistemas de Formatação (AMBOS disponíveis) -----
    
    /**
     * Formata mensagens com cores tradicionais (&a, &6, etc)
     */
    public String formatMessageLegacy(String message) {
        return ChatColor.translateAlternateColorCodes('&', 
            getMessages().getString("prefix", "&6[Magnata] &7") + message
        );
    }
    
    /**
     * Formata mensagens com MiniMessage (<gold>, <gradient>, etc)
     */
    public Component formatComponent(String message) {
        return miniMessage.deserialize(
            getMessages().getString("prefix", "<gold>[Magnata]</gold> <gray>") + message
        );
    }
    
    // ----- Métodos Originais (mantidos integralmente) -----
    
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

    // ... (todos os outros métodos originais permanecem IDÊNTICOS)
    
    // Getters
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public Economy getEconomy() { return economy; }
    public FileConfiguration getMessages() { return messages; }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}

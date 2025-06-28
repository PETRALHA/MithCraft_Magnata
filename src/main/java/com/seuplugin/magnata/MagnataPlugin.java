package com.mithcraft.magnata;

import me.clip.placeholderapi.PlaceholderAPI;
import net.ess3.api.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MagnataPlugin extends JavaPlugin {
    private Economy economy;
    private LuckPerms luckPerms;
    private HistoryManager historyManager;
    private RewardManager rewardManager;
    private boolean papiEnabled;

    @Override
    public void onEnable() {
        // Configuração inicial
        saveDefaultConfig();
        saveResource("history.yml", false);

        // Verificar dependências
        if (!setupEconomy() || !setupLuckPerms()) {
            getLogger().severe("Dependências (EssentialsX/LuckPerms) não encontradas!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // PlaceholderAPI (opcional)
        papiEnabled = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            new MagnataExpansion(this).register();
            getLogger().info("PlaceholderAPI integrado com sucesso!");
        }

        // Inicializar managers
        historyManager = new HistoryManager(this);
        rewardManager = new RewardManager(this);

        // Registrar comandos
        getCommand("magnata").setExecutor(new MagnataCommand(this));
        getCommand("magnata").setTabCompleter(new MagnataTabCompleter());
        getCommand("magnata hist").setExecutor(new MagnataHistoryCommand(this));
        getCommand("magnata help").setExecutor(new MagnataHelpCommand(this));
    }

    // Métodos auxiliares (setupEconomy, setupLuckPerms, etc...)
    public boolean isPapiEnabled() { return papiEnabled; }
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}
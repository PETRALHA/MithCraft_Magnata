package com.mithcraft.magnata;

import me.clip.placeholderapi.PlaceholderAPI;
import net.ess3.api.Economy;
import net.luckperms.api.LuckPerms;
import org.bukkit.OfflinePlayer;
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
        saveDefaultConfig();
        saveResource("history.yml", false);

        if (!setupEconomy() || !setupLuckPerms()) {
            getLogger().severe("Dependências (EssentialsX/LuckPerms) não encontradas!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        papiEnabled = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            new MagnataExpansion(this).register();
        }

        historyManager = new HistoryManager(this);
        rewardManager = new RewardManager(this);

        getCommand("magnata").setExecutor(new MagnataCommand(this));
        getCommand("magnata hist").setExecutor(new MagnataHistoryCommand(this));
        getCommand("magnata help").setExecutor(new MagnataHelpCommand(this));
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> rsp = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (rsp == null) return false;
        luckPerms = rsp.getProvider();
        return luckPerms != null;
    }

    // Getters
    public boolean isPapiEnabled() { return papiEnabled; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public HistoryManager getHistoryManager() { return historyManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}
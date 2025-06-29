package com.mithcraft.magnata;

import net.luckperms.api.LuckPerms;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class MagnataPlugin extends JavaPlugin {
    private LuckPerms luckPerms;
    private boolean papiEnabled;

    @Override
    public void onEnable() {
        // LuckPerms com fallback
        try {
            luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        } catch (NoClassDefFoundError e) {
            getLogger().warning("LuckPerms não encontrado! Algumas features serão desativadas.");
        }

        // PlaceholderAPI com verificação segura
        papiEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiEnabled) {
            new MagnataExpansion(this).register();
        }
    }
}
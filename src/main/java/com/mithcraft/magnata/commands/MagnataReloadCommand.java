package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MagnataReloadCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataReloadCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Verificação de permissão
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.magnata_reload", "magnata.reload"))) {
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
            return true;
        }

        try {
            // Recarregar configurações
            plugin.reloadConfig();
            plugin.getMessages().reload();
            plugin.getHistoryManager().reload();
            
            // Verificar integrações
            plugin.setupEconomy();
            plugin.setupPlaceholderAPI();
            
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.reload_success")));
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao recarregar o plugin: " + e.getMessage());
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.reload_failure")));
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
        }
        
        return true;
    }
}
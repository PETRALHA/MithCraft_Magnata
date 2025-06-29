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
            // Processo de recarregamento completo
            plugin.reloadConfig(); // Recarrega config.yml
            plugin.loadConfigurations(); // Recarrega messages.yml
            
            // Recarrega managers
            plugin.getHistoryManager().reload();
            plugin.getRewardManager().reload();
            
            // Reconecta com dependências
            plugin.setupEconomy();
            plugin.setupPlaceholderAPI();

            // Mensagem de sucesso
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.reload_success")));
            return true;
        } catch (Exception e) {
            // Tratamento de erro detalhado
            plugin.getLogger().severe("Erro durante o reload: " + e.getMessage());
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.reload_failure")));
            
            // Log adicional no modo debug
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
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
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.magnata_reload", "magnata.reload"))) {
            sender.sendMessage(plugin.getMessages().getString("prefix") + "§cVocê não tem permissão para recarregar o plugin.");
            return true;
        }

        plugin.reload();
        sender.sendMessage(plugin.getMessages().getString("prefix") + "§aPlugin recarregado com sucesso!");
        return true;
    }
}
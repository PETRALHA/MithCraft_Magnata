package com.mithcraft.magnata.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MagnataHelpCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.magnata_help", "magnata.help"))) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para este comando.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Ajuda do MithCraft Magnata ===");
        sender.sendMessage(ChatColor.YELLOW + "/magnata - Mostra o magnata atual");
        sender.sendMessage(ChatColor.YELLOW + "/magnata hist - Mostra o histórico");
        sender.sendMessage(ChatColor.YELLOW + "/magnata reload - Recarrega o plugin");
        sender.sendMessage(ChatColor.YELLOW + "/magnata help - Mostra esta ajuda");
        return true;
    }
}
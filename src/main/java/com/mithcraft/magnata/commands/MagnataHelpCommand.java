package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
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
        if (!sender.hasPermission("magnata.help")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para ver a ajuda.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Ajuda do MithCraft Magnata ===");
        sender.sendMessage(ChatColor.YELLOW + "/magnata - Mostra o magnata atual");
        sender.sendMessage(ChatColor.YELLOW + "/magnata hist - Mostra o histórico");
        sender.sendMessage(ChatColor.YELLOW + "/magnata help - Mostra esta ajuda");
        return true;
    }
}
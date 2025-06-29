package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MagnataHistoryCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHistoryCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("magnata.history")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para ver o histórico.");
            return true;
        }

        // Implementação do histórico aqui
        sender.sendMessage(ChatColor.GOLD + "Histórico de magnatas:");
        plugin.getHistoryManager().getHistory().forEach(entry -> {
            sender.sendMessage(ChatColor.YELLOW + "- " + entry.getPlayerName() + ": $" + entry.getBalance());
        });
        return true;
    }
}
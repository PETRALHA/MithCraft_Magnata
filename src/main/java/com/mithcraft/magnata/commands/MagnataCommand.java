package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MagnataCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("magnata.command")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }
        
        // Implementação do comando aqui
        sender.sendMessage(ChatColor.GOLD + "Comando /magnata funcionando!");
        return true;
    }
}
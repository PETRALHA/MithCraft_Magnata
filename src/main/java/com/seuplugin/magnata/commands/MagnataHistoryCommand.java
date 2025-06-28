package com.seuplugin.magnata.commands;

import com.seuplugin.magnata.MagnataPlugin;
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
        sender.sendMessage("Comando /magnata hist funcionando!");
        return true;
    }
}
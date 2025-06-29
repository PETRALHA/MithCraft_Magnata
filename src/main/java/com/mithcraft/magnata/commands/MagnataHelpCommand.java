package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagnataHelpCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("magnata.help")) {
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
            return true;
        }

        List<String> helpMessages = plugin.getMessages().getStringList("help");
        
        for (String line : helpMessages) {
            sender.sendMessage(plugin.formatMessage(line));
        }
        return true;
    }
}
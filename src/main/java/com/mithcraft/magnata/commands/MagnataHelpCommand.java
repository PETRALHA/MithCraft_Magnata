package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MagnataHelpCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> helpMessages = plugin.getConfig().getStringList("messages.help");
        helpMessages.forEach(sender::sendMessage);
        return true;
    }
}
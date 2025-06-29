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
        String prefix = plugin.getMessages().getString("prefix", "&6[Magnata] &7");
        List<String> helpMessages = plugin.getMessages().getStringList("help");
        
        helpMessages.forEach(line -> sender.sendMessage(line.replace("%prefix%", prefix)));
        return true;
    }
}
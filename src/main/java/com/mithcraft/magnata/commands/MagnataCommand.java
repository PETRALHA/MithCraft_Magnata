package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MagnataCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return showCurrentMagnata(sender);
        }

        switch (args[0].toLowerCase()) {
            case "help":
                if (!sender.hasPermission("magnata.help")) {
                    sender.sendMessage(plugin.formatMessage("no_permission"));
                    return true;
                }
                return new MagnataHelpCommand(plugin).onCommand(sender, cmd, label, args);
                
            case "history":
            case "hist":
            case "list":
                if (!sender.hasPermission("magnata.history")) {
                    sender.sendMessage(plugin.formatMessage("no_permission"));
                    return true;
                }
                return new MagnataHistoryCommand(plugin).onCommand(sender, cmd, label, args);
                
            case "reload":
                if (!sender.hasPermission("magnata.reload")) {
                    sender.sendMessage(plugin.formatMessage("no_permission"));
                    return true;
                }
                return new MagnataReloadCommand(plugin).onCommand(sender, cmd, label, args);
                
            default:
                return showCurrentMagnata(sender);
        }
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!sender.hasPermission("magnata.command")) {
            sender.sendMessage(plugin.formatMessage("no_permission"));
            return true;
        }

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) {
            sender.sendMessage(plugin.formatMessage("no_magnata"));
            return true;
        }

        sender.sendMessage(plugin.formatMessage("current_magnata")
            .replace("%player%", current.getPlayerName())
            .replace("%balance%", plugin.formatCurrency(current.getBalance()))
            .replace("%date%", current.getFormattedDate()));
        
        return true;
    }
}
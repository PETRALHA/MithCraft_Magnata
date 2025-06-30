package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MagnataCommand implements CommandExecutor {
    private final MagnataPlugin plugin;
    private final MagnataHelpCommand helpCommand;
    private final MagnataHistoryCommand historyCommand;
    private final MagnataReloadCommand reloadCommand;

    public MagnataCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.helpCommand = new MagnataHelpCommand(plugin);
        this.historyCommand = new MagnataHistoryCommand(plugin);
        this.reloadCommand = new MagnataReloadCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return showCurrentMagnata(sender);
        }

        String subCommand = args[0].toLowerCase();
        String permission = getPermissionForSubCommand(subCommand);

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        return executeSubCommand(subCommand, sender, cmd, label, args);
    }

    private boolean executeSubCommand(String subCommand, CommandSender sender, Command cmd, String label, String[] args) {
        switch (subCommand) {
            case "help":
                return helpCommand.onCommand(sender, cmd, label, args);
                
            case "hist":
            case "list":
            case "history":
                return historyCommand.onCommand(sender, cmd, label, args);
                
            case "reload":
                return reloadCommand.onCommand(sender, cmd, label, args);
                
            default:
                return showCurrentMagnata(sender);
        }
    }

    private String getPermissionForSubCommand(String subCommand) {
        switch (subCommand) {
            case "help":
                return plugin.getConfig().getString("permissions.help", "magnata.help");
            case "hist":
            case "list":
            case "history":
                return plugin.getConfig().getString("permissions.history", "magnata.history");
            case "reload":
                return plugin.getConfig().getString("permissions.reload", "magnata.reload");
            default:
                return plugin.getConfig().getString("permissions.base", "magnata.command");
        }
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.base", "magnata.command"))) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) {
            sender.sendMessage(plugin.formatMessage("magnata.none_defined"));
            return true;
        }

        plugin.getMessages().getStringList("commands.magnata.current.content").forEach(line -> 
            sender.sendMessage(plugin.formatMessage(line)
                .replace("%player%", current.getPlayerName())
                .replace("%balance%", plugin.formatCurrency(current.getBalance()))
                .replace("%date%", current.getFormattedDate())
            )
        );
        return true;
    }
}
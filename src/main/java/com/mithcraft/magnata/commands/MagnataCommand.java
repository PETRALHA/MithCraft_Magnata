package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MagnataCommand implements CommandExecutor, TabCompleter {
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

        switch (args[0].toLowerCase()) {
            case "help":
                if (!sender.hasPermission(plugin.getConfig().getString("permissions.help", "magnata.help"))) {
                    sender.sendMessage(plugin.formatMessage("errors.no_permission"));
                    return true;
                }
                return helpCommand.onCommand(sender, cmd, label, args);
                
            case "hist":
            case "list":
            case "history":
                if (!sender.hasPermission(plugin.getConfig().getString("permissions.history", "magnata.history"))) {
                    sender.sendMessage(plugin.formatMessage("errors.no_permission"));
                    return true;
                }
                return historyCommand.onCommand(sender, cmd, label, args);
                
            case "reload":
                if (!sender.hasPermission(plugin.getConfig().getString("permissions.reload", "magnata.reload"))) {
                    sender.sendMessage(plugin.formatMessage("errors.no_permission"));
                    return true;
                }
                return reloadCommand.onCommand(sender, cmd, label, args);
                
            default:
                return showCurrentMagnata(sender);
        }
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.base", "magnata.command"))) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        if (plugin.getHistoryManager().getCurrentMagnata() == null) {
            sender.sendMessage(plugin.formatMessage("&eNenhum magnata definido ainda."));
            return true;
        }

        plugin.getMessages().getStringList("current_magnata").forEach(line -> 
            sender.sendMessage(plugin.formatMessage(line)
                .replace("%player%", plugin.getHistoryManager().getCurrentMagnata().getPlayerName())
                .replace("%balance%", plugin.formatCurrency(plugin.getHistoryManager().getCurrentMagnata().getBalance()))
                .replace("%date%", plugin.getHistoryManager().getCurrentMagnata().getFormattedDate())
            )
        );
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission(plugin.getConfig().getString("permissions.help", "magnata.help"))) {
                completions.add("help");
            }
            if (sender.hasPermission(plugin.getConfig().getString("permissions.history", "magnata.history"))) {
                completions.add("history");
                completions.add("hist");
                completions.add("list");
            }
            if (sender.hasPermission(plugin.getConfig().getString("permissions.reload", "magnata.reload"))) {
                completions.add("reload");
            }
        }
        
        return completions;
    }
}

package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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
        // Verificar permissão básica
        if (!sender.hasPermission("magnata.command")) {
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
            return true;
        }

        if (args.length == 0) {
            return helpCommand.onCommand(sender, cmd, label, args);
        }

        switch (args[0].toLowerCase()) {
            case "help":
                return helpCommand.onCommand(sender, cmd, label, args);
            case "hist":
            case "history":
                if (!sender.hasPermission("magnata.history")) {
                    sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
                    return true;
                }
                return historyCommand.onCommand(sender, cmd, label, args);
            case "reload":
                if (!sender.hasPermission("magnata.reload")) {
                    sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
                    return true;
                }
                return reloadCommand.onCommand(sender, cmd, label, args);
            default:
                sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.unknown_command")));
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("magnata.command")) completions.add("help");
            if (sender.hasPermission("magnata.history")) completions.add("history");
            if (sender.hasPermission("magnata.reload")) completions.add("reload");
        }
        
        return completions;
    }
}

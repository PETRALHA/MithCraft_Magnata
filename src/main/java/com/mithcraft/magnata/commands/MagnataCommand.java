package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

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
            return helpCommand.onCommand(sender, cmd, label, args);
        }

        switch (args[0].toLowerCase()) {
            case "help":
                return helpCommand.onCommand(sender, cmd, label, args);
            case "hist":
            case "history":
                return historyCommand.onCommand(sender, cmd, label, args);
            case "reload":
                return reloadCommand.onCommand(sender, cmd, label, args);
            default:
                sender.sendMessage(plugin.getMessages().getString("prefix", "&6[Magnata] &7") + 
                                 plugin.getMessages().getString("errors.unknown_command", "&cComando desconhecido"));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("history");
            completions.add("reload");
        }
        return completions;
    }
}
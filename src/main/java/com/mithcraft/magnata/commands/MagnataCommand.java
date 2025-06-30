package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MagnataCommand implements CommandExecutor, TabCompleter {
    private final MagnataPlugin plugin;
    private final MagnataHelpCommand helpCommand;
    private final MagnataHistoryCommand historyCommand;
    private final MagnataReloadCommand reloadCommand;

    public MagnataCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.helpCommand = new MagnataHelpCommand(plugin);
        this.historyCommand = new MagnataHistoryCommand(plugin);
        this.reloadCommand = new MagnataReloadCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            if (args.length == 0) {
                return showCurrentMagnata(sender);
            }

            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "help":
                    return checkPermissionAndExecute(sender, "help", () -> 
                        helpCommand.onCommand(sender, cmd, label, args));
                    
                case "hist":
                case "list":
                case "history":
                    return checkPermissionAndExecute(sender, "history", () -> 
                        historyCommand.onCommand(sender, cmd, label, args));
                    
                case "reload":
                    return checkPermissionAndExecute(sender, "reload", () -> 
                        reloadCommand.onCommand(sender, cmd, label, args));
                    
                default:
                    return showCurrentMagnata(sender);
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao executar comando magnata", e);
            sender.sendMessage(plugin.formatMessage("errors.command_failed"));
            return true;
        }
    }

    private boolean checkPermissionAndExecute(CommandSender sender, String permissionType, CommandAction action) {
        String permission = plugin.getConfig().getString("permissions." + permissionType, "magnata." + permissionType);
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }
        return action.execute();
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        String permission = plugin.getConfig().getString("permissions.base", "magnata.command");
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        if (current == null) {
            sender.sendMessage(plugin.formatMessage("magnata.none_defined"));
            return true;
        }

        plugin.getMessages().getStringList("current_magnata").forEach(line -> 
            sender.sendMessage(plugin.formatMessage(line)
                .replace("%player%", current.getPlayerName())
                .replace("%balance%", plugin.formatCurrency(current.getBalance()))
                .replace("%date%", current.getFormattedDate())
        ));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        addCompletionIfPermitted(sender, completions, "help");
        addCompletionIfPermitted(sender, completions, "history", "hist", "list");
        addCompletionIfPermitted(sender, completions, "reload");
        
        return completions;
    }

    private void addCompletionIfPermitted(CommandSender sender, List<String> completions, String... options) {
        String permissionType = options[0];
        String permission = plugin.getConfig().getString("permissions." + permissionType, "magnata." + permissionType);
        
        if (sender.hasPermission(permission)) {
            Collections.addAll(completions, options);
        }
    }

    @FunctionalInterface
    private interface CommandAction {
        boolean execute();
    }
}

package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class MagnataCommand implements CommandExecutor, TabCompleter {
    private final MagnataPlugin plugin;
    
    // Permissões hardcoded
    private static final String PERMISSION_BASE = "magnata.command";
    private static final String PERMISSION_HELP = "magnata.help";
    private static final String PERMISSION_HISTORY = "magnata.history";
    private static final String PERMISSION_RELOAD = "magnata.reload";
    private static final String PERMISSION_NOTIFY = "magnata.notify";

    public MagnataCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return showCurrentMagnata(sender);
        }

        switch (args[0].toLowerCase()) {
            case "help":
            case "ajuda":
            case "?":
                return executeHelpCommand(sender);
                
            case "history":
            case "hist":
            case "list":
                return executeHistoryCommand(sender);
                
            case "reload":
                return executeReloadCommand(sender);
                
            default:
                return handleInvalidCommand(sender);
        }
    }

    private boolean executeHelpCommand(CommandSender sender) {
        if (!checkPermission(sender, PERMISSION_HELP)) return true;
        return new MagnataHelpCommand(plugin).onCommand(sender, null, null, null);
    }

    private boolean executeHistoryCommand(CommandSender sender) {
        if (!checkPermission(sender, PERMISSION_HISTORY)) return true;
        return new MagnataHistoryCommand(plugin).onCommand(sender, null, null, null);
    }

    private boolean executeReloadCommand(CommandSender sender) {
        if (!checkPermission(sender, PERMISSION_RELOAD)) return true;
        
        try {
            plugin.reloadConfig();
            plugin.loadConfigurations();
            plugin.setupEconomy();
            plugin.getHistoryManager().reload();
            plugin.getRewardManager().reload();
            sendColoredMessage(sender, "commands.magnata.reload.success");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao recarregar:", e);
            sendColoredMessage(sender, "commands.magnata.reload.failure");
            return false;
        }
    }

    private boolean handleInvalidCommand(CommandSender sender) {
        sendColoredMessage(sender, "errors.invalid_command");
        return true;
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!checkPermission(sender, PERMISSION_BASE)) return true;

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        String prefix = getFormattedPrefix();
        
        sendColoredMessage(sender, "commands.magnata.current.header");
        
        if (current == null) {
            sendColoredMessage(sender, "commands.magnata.current.empty");
        } else {
            List<String> contentLines = plugin.getMessages().getStringList("commands.magnata.current.content");
            for (String line : contentLines) {
                String formatted = plugin.colorize(line
                    .replace("{prefix}", prefix)
                    .replace("{player}", current.getPlayerName())
                    .replace("{balance}", plugin.formatCurrency(current.getBalance()))
                    .replace("{date}", current.getFormattedDate()));
                sender.sendMessage(formatted);
            }
        }
        
        sendColoredMessage(sender, "commands.magnata.current.footer");
        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sendColoredMessage(sender, "errors.no_permission");
        return false;
    }

    private void sendColoredMessage(CommandSender sender, String messagePath) {
        String message = plugin.getMessages().getString(messagePath, "");
        if (!message.isEmpty()) {
            sender.sendMessage(plugin.colorize(
                message.replace("{prefix}", getFormattedPrefix())
            ));
        }
    }

    private String getFormattedPrefix() {
        return plugin.colorize(plugin.getMessages().getString("formats.prefix", ""));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission(PERMISSION_HELP)) completions.add("help");
            if (sender.hasPermission(PERMISSION_HISTORY)) completions.add("history");
            if (sender.hasPermission(PERMISSION_RELOAD)) completions.add("reload");
            return completions;
        }
        return new ArrayList<>();
    }
}
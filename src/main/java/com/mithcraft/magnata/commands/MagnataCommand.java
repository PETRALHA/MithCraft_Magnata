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
                return handleInvalidCommand(sender, args);
        }
    }

    private boolean executeHelpCommand(CommandSender sender) {
        if (!checkPermission(sender, "magnata.help")) return true;
        return new MagnataHelpCommand(plugin).onCommand(sender, null, null, null);
    }

    private boolean executeHistoryCommand(CommandSender sender) {
        if (!checkPermission(sender, "magnata.history")) return true;
        return new MagnataHistoryCommand(plugin).onCommand(sender, null, null, null);
    }

    private boolean executeReloadCommand(CommandSender sender) {
        if (!checkPermission(sender, "magnata.reload")) return true;
        
        try {
            plugin.reloadConfig();
            if (plugin.loadConfigurations() && plugin.setupEconomy()) {
                plugin.getHistoryManager().reload();
                plugin.getRewardManager().reload();
                sender.sendMessage(getMessage("commands.magnata.reload.success"));
                return true;
            }
            sender.sendMessage(getMessage("commands.magnata.reload.failure"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao recarregar:", e);
            sender.sendMessage(getMessage("commands.magnata.reload.failure"));
        }
        return true;
    }

    private boolean handleInvalidCommand(CommandSender sender, String[] args) {
        if (!args[0].isEmpty()) {
            sender.sendMessage(getMessage("errors.invalid_command"));
        }
        return showCurrentMagnata(sender);
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!checkPermission(sender, "magnata.command")) return true;

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        String prefix = getPrefix();
        
        // Header
        sender.sendMessage(getMessage("commands.magnata.current.header"));
        
        // Content
        if (current == null) {
            sender.sendMessage(getMessage("commands.magnata.current.empty"));
        } else {
            List<String> contentLines = plugin.getMessages().getStringList("commands.magnata.current.content");
            for (String line : contentLines) {
                sender.sendMessage(line
                    .replace("{prefix}", prefix)
                    .replace("{player}", current.getPlayerName())
                    .replace("{balance}", plugin.formatCurrency(current.getBalance()))
                    .replace("{date}", current.getFormattedDate()));
            }
        }
        
        // Footer
        sender.sendMessage(getMessage("commands.magnata.current.footer"));
        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(getMessage("errors.no_permission"));
        return false;
    }

    private String getMessage(String path) {
        return plugin.getMessages().getString(path, "&cMensagem não configurada: " + path)
            .replace("{prefix}", getPrefix());
    }

    private String getPrefix() {
        return plugin.getMessages().getString("formats.prefix", "");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("magnata.help")) completions.add("help");
            if (sender.hasPermission("magnata.history")) completions.add("history");
            if (sender.hasPermission("magnata.reload")) completions.add("reload");
            return completions;
        }
        return new ArrayList<>();
    }
}
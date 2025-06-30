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
import java.util.logging.Level;

public class MagnataCommand implements CommandExecutor, TabCompleter {
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
            case "ajuda":
            case "?":
                if (!checkPermission(sender, "magnata.help")) return true;
                return new MagnataHelpCommand(plugin).onCommand(sender, cmd, label, args);
                
            case "history":
            case "hist":
            case "list":
                if (!checkPermission(sender, "magnata.history")) return true;
                return new MagnataHistoryCommand(plugin).onCommand(sender, cmd, label, args);
                
            case "reload":
                if (!checkPermission(sender, "magnata.reload")) return true;
                return handleReload(sender);
                
            default:
                if (!args[0].isEmpty()) {
                    sender.sendMessage(plugin.formatMessage("errors.invalid_command"));
                }
                return showCurrentMagnata(sender);
        }
    }

    private boolean handleReload(CommandSender sender) {
        try {
            plugin.reloadConfig();
            if (plugin.loadConfigurations() && plugin.setupEconomy()) {
                sender.sendMessage(plugin.formatMessage("commands.magnata.reload.success"));
                return true;
            }
            sender.sendMessage(plugin.formatMessage("commands.magnata.reload.failure"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao recarregar:", e);
            sender.sendMessage(plugin.formatMessage("commands.magnata.reload.failure"));
        }
        return true;
    }

    private boolean showCurrentMagnata(CommandSender sender) {
        if (!checkPermission(sender, "magnata.command")) return true;

        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        
        // Header
        sender.sendMessage(plugin.formatMessage("commands.magnata.current.header"));
        
        // Conteúdo
        if (current == null) {
            sender.sendMessage(plugin.formatMessage("commands.magnata.current.empty"));
        } else {
            List<String> contentLines = plugin.getMessages().getStringList("commands.magnata.current.content");
            for (String line : contentLines) {
                String formatted = plugin.formatMessage(line)
                    .replace("{player}", current.getPlayerName())
                    .replace("{balance}", plugin.formatCurrency(current.getBalance()))
                    .replace("{date}", current.getFormattedDate());
                sender.sendMessage(formatted);
            }
        }
        
        // Footer
        sender.sendMessage(plugin.formatMessage("commands.magnata.current.footer"));
        return true;
    }

    private void sendFormattedContent(CommandSender sender, MagnataRecord record) {
        List<String> contentLines = plugin.getMessages().getStringList("commands.magnata.current.content");
        if (contentLines.isEmpty()) {
            sendFormattedMessage(sender, "commands.magnata.current.empty");
            return;
        }

        contentLines.forEach(line -> {
            String formatted = plugin.formatMessage(line)
                .replace("%player%", record.getPlayerName())
                .replace("%balance%", plugin.formatCurrency(record.getBalance()))
                .replace("%date%", record.getFormattedDate());
            sender.sendMessage(formatted);
        });
    }

    private void sendFormattedMessage(CommandSender sender, String path) {
        String message = plugin.formatMessage(path);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return false;
        }
        return true;
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
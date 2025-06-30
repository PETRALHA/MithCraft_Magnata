package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MagnataHelpCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin instance cannot be null");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Check permission using config-defined permission node
        String permissionNode = plugin.getConfig().getString("permissions.help", "magnata.help");
        if (!sender.hasPermission(permissionNode)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        sendFormattedHelp(sender);
        return true;
    }

    private void sendFormattedHelp(CommandSender sender) {
        // Send header
        String header = plugin.formatMessage("help.header");
        if (!header.isEmpty()) {
            sender.sendMessage(header);
        }

        // Process help content
        List<String> helpContent = plugin.getMessages().getStringList("help.content");
        if (helpContent.isEmpty()) {
            sender.sendMessage(plugin.formatMessage("help.empty"));
        } else {
            boolean filterByPermission = plugin.getConfig().getBoolean("settings.help_command.show_permission_restricted", false);
            
            helpContent.forEach(line -> {
                if (shouldDisplayLine(sender, line, filterByPermission)) {
                    sender.sendMessage(plugin.formatMessage(line));
                }
            });
        }

        // Send footer
        String footer = plugin.formatMessage("help.footer");
        if (!footer.isEmpty()) {
            sender.sendMessage(footer);
        }
    }

    private boolean shouldDisplayLine(CommandSender sender, String line, boolean filterByPermission) {
        if (!filterByPermission) return true;
        
        // Extract permission from placeholder {permission} in the line
        if (line.contains("{permission}")) {
            String permission = line.split("\\{permission}")[1].split("\\}")[0];
            return sender.hasPermission(permission.trim());
        }
        return true;
    }
}
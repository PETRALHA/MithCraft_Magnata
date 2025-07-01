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
    private static final String HELP_PERMISSION = "magnata.help"; // Permissão hardcoded

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }

        sendHelpMessages(sender);
        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(HELP_PERMISSION)) {
            return true;
        }
        sendColoredMessage(sender, "errors.no_permission");
        return false;
    }

    private void sendHelpMessages(CommandSender sender) {
        String prefix = plugin.getPrefix();
        
        // Header
        sendColoredMessage(sender, "help.header");

        // Conteúdo
        List<String> helpLines = plugin.getMessages().getStringList("help.content");
        if (helpLines.isEmpty()) {
            sendColoredMessage(sender, "help.empty");
        } else {
            helpLines.forEach(line -> 
                sender.sendMessage(plugin.colorize(line.replace("{prefix}", prefix)))
            );
        }

        // Footer
        sendColoredMessage(sender, "help.footer");
    }

    private void sendColoredMessage(CommandSender sender, String messagePath) {
        String message = plugin.getMessages().getString(messagePath, "");
        if (!message.isEmpty()) {
            sender.sendMessage(plugin.colorize(
                message.replace("{prefix}", plugin.getPrefix())
            ));
        }
    }
}
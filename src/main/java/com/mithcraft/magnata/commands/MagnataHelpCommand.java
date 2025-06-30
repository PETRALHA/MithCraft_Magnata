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
    private final String helpPermission;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.helpPermission = plugin.getConfig().getString("permissions.help", "magnata.help");
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
        if (sender.hasPermission(helpPermission)) {
            return true;
        }
        String noPermMessage = plugin.getMessages().getString("errors.no_permission")
            .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""));
        sender.sendMessage(noPermMessage);
        return false;
    }

    private void sendHelpMessages(CommandSender sender) {
        String prefix = plugin.getMessages().getString("formats.prefix", "");
        
        // Envia header
        sendSection(sender, "help.header", prefix);
        
        // Envia conteúdo
        List<String> helpLines = plugin.getMessages().getStringList("help.content");
        if (helpLines.isEmpty()) {
            sendSection(sender, "help.empty", prefix);
        } else {
            helpLines.forEach(line -> sender.sendMessage(line.replace("{prefix}", prefix)));
        }
        
        // Envia footer
        sendSection(sender, "help.footer", prefix);
    }

    private void sendSection(CommandSender sender, String path, String prefix) {
        if (plugin.getMessages().isList(path)) {
            plugin.getMessages().getStringList(path).forEach(line -> 
                sender.sendMessage(line.replace("{prefix}", prefix))
            );
        } else {
            String message = plugin.getMessages().getString(path, "")
                .replace("{prefix}", prefix);
            if (!message.isEmpty()) {
                sender.sendMessage(message);
            }
        }
    }
}
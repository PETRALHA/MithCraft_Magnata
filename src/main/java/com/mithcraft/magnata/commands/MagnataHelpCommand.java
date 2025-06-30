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
        sender.sendMessage(plugin.colorize(
            plugin.getMessages().getString("errors.no_permission", "&cSem permissão!")
                .replace("{prefix}", plugin.getPrefix())
        ));
        return false;
    }

    private void sendHelpMessages(CommandSender sender) {
        String prefix = plugin.getPrefix();
        
        // Envia header
        sender.sendMessage(plugin.colorize(
            plugin.getMessages().getString("help.header", "&8[&6Ajuda&8]")
                .replace("{prefix}", prefix)
        ));

        // Envia conteúdo
        List<String> helpLines = plugin.getMessages().getStringList("help.content");
        if (helpLines.isEmpty()) {
            sender.sendMessage(plugin.colorize(
                plugin.getMessages().getString("help.empty", "&cNenhum comando disponível")
                    .replace("{prefix}", prefix)
            ));
        } else {
            helpLines.forEach(line -> 
                sender.sendMessage(plugin.colorize(line.replace("{prefix}", prefix)))
            );
        }

        // Envia footer
        sender.sendMessage(plugin.colorize(
            plugin.getMessages().getString("help.footer", "&8----------------")
                .replace("{prefix}", prefix)
        ));
    }
}
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
        if (!sender.hasPermission(helpPermission)) {
            sender.sendMessage(plugin.formatMessage("errors.no_permission"));
            return true;
        }

        sendHelpMessages(sender);
        return true;
    }

    private void sendHelpMessages(CommandSender sender) {
        // Envia cabeçalho
        sendSection(sender, "help.header");

        // Envia conteúdo
        List<String> helpLines = plugin.getMessages().getStringList("help.content");
        if (helpLines.isEmpty()) {
            sender.sendMessage(plugin.formatMessage("help.empty"));
        } else {
            boolean filterByPermission = plugin.getConfig().getBoolean("settings.help_command.show_permission_restricted", false);
            
            helpLines.forEach(line -> {
                if (!filterByPermission || hasRequiredPermission(sender, line)) {
                    sender.sendMessage(plugin.formatMessage(line));
                }
            });
        }

        // Envia rodapé
        sendSection(sender, "help.footer");
    }

    private boolean hasRequiredPermission(CommandSender sender, String line) {
        if (!line.contains("{permission}")) return true;
        
        try {
            String permission = line.split("\\{permission}")[1].split("\\}")[0].trim();
            return permission.isEmpty() || sender.hasPermission(permission);
        } catch (Exception e) {
            plugin.getLogger().warning("Formato de permissão inválido na linha de ajuda: " + line);
            return true;
        }
    }

    private void sendSection(CommandSender sender, String path) {
        String message = plugin.formatMessage(path);
        if (!message.trim().isEmpty()) {
            sender.sendMessage(message);
        }
    }
}
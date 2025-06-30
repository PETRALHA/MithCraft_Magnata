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
    private final String permission;

    public MagnataHelpCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.permission = plugin.getConfig().getString("permissions.help", "magnata.help");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(plugin.formatMessage(getMessage("errors.no_permission")));
                return true;
            }

            sendHelpMessages(sender);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao executar comando help: " + e.getMessage());
            sender.sendMessage(plugin.formatMessage(getMessage("errors.command_failed")));
            return true;
        }
    }

    private void sendHelpMessages(CommandSender sender) {
        List<String> helpMessages = getMessageList("help");
        if (helpMessages.isEmpty()) {
            sender.sendMessage(plugin.formatMessage(getMessage("help.empty")));
            return;
        }

        helpMessages.forEach(message -> 
            sender.sendMessage(plugin.formatMessage(message))
        );
    }

    private String getMessage(String path) {
        return plugin.getMessages().getString(path, "&cMensagem não configurada: " + path);
    }

    private List<String> getMessageList(String path) {
        return plugin.getMessages().getStringList(path);
    }
}

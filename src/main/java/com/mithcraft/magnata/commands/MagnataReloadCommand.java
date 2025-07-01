package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MagnataReloadCommand implements CommandExecutor {
    private final MagnataPlugin plugin;
    private static final String RELOAD_PERMISSION = "magnata.reload"; // Permissão hardcoded

    public MagnataReloadCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        return executeReload(sender);
    }

    private boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(RELOAD_PERMISSION)) {
            return true;
        }
        sendColoredMessage(sender, "errors.no_permission");
        return false;
    }

    private boolean executeReload(CommandSender sender) {
        try {
            reloadPluginComponents();
            sendColoredMessage(sender, "commands.magnata.reload.success");
            return true;
        } catch (Exception e) {
            handleReloadError(sender, e);
            return false;
        }
    }

    private void reloadPluginComponents() throws Exception {
        // 1. Recarregar configurações
        plugin.reloadConfig();
        plugin.loadConfigurations();

        // 2. Reiniciar managers
        plugin.getHistoryManager().reload();
        plugin.getRewardManager().reload();

        // 3. Verificar dependências críticas
        if (!plugin.setupEconomy()) {
            throw new IllegalStateException("Falha ao reconectar com Vault/Economy");
        }
        plugin.setupPlaceholderAPI();
    }

    private void handleReloadError(CommandSender sender, Exception e) {
        plugin.getLogger().severe("Erro no reload: " + e.getMessage());
        sendColoredMessage(sender, "commands.magnata.reload.failure");

        if (isDebugMode()) {
            e.printStackTrace();
        }
    }

    private void sendColoredMessage(CommandSender sender, String messagePath) {
        String message = plugin.getMessages().getString(messagePath, "");
        if (!message.isEmpty()) {
            sender.sendMessage(plugin.colorize(
                message.replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
            ));
        }
    }

    private boolean isDebugMode() {
        return plugin.getConfig().getBoolean("settings.debug", false);
    }
}
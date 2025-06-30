package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MagnataReloadCommand implements CommandExecutor {
    private final MagnataPlugin plugin;
    private final String permission;

    public MagnataReloadCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.permission = plugin.getConfig().getString("permissions.reload", "magnata.reload");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }

        return executeReload(sender);
    }

    private boolean hasPermission(CommandSender sender) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage(getMessage("errors.no_permission")));
            return false;
        }
        return true;
    }

    private boolean executeReload(CommandSender sender) {
        try {
            reloadPluginComponents();
            sender.sendMessage(plugin.formatMessage(getMessage("reload.success")));
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

        // 3. Reconectar dependências
        verifyDependencies();
    }

    private void verifyDependencies() {
        if (!plugin.setupEconomy()) {
            throw new IllegalStateException("Falha ao reconectar com Vault/Economy");
        }
        plugin.setupPlaceholderAPI();
    }

    private void handleReloadError(CommandSender sender, Exception e) {
        String errorMessage = getMessage("reload.failure");
        plugin.getLogger().severe("Erro no reload: " + e.getMessage());
        sender.sendMessage(plugin.formatMessage(errorMessage));

        if (isDebugMode()) {
            e.printStackTrace();
        }
    }

    private String getMessage(String path) {
        return plugin.getMessages().getString(path, "&c" + path.replace(".", "_"));
    }

    private boolean isDebugMode() {
        return plugin.getConfig().getBoolean("settings.debug", false);
    }
}

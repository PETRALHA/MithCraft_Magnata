package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MagnataCommand implements CommandExecutor, TabCompleter {
    private final MagnataHelpCommand helpCommand;
    private final MagnataHistoryCommand historyCommand;
    private final MagnataReloadCommand reloadCommand;

    public MagnataCommand(MagnataPlugin plugin) {
        this.helpCommand = new MagnataHelpCommand(plugin);
        this.historyCommand = new MagnataHistoryCommand(plugin);
        this.reloadCommand = new MagnataReloadCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        String prefix = plugin.getMessages().getString("prefix", "&6[Magnata] &7");
        
        if (!sender.hasPermission(plugin.getMainConfig().getString("permissions.magnata_reload", "magnata.reload"))) {
            sender.sendMessage(prefix + plugin.getMessages().getString("errors.no_permission", "&cVocê não tem permissão!"));
            return true;
        }

        plugin.reload();
        sender.sendMessage(prefix + plugin.getMessages().getString("reload_success", "&aConfigurações recarregadas!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("history");
            completions.add("reload");
        }
        return completions;
    }
}
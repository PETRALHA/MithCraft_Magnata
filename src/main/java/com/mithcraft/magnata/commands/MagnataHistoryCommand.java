package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagnataHistoryCommand implements CommandExecutor {
    private final MagnataPlugin plugin;

    public MagnataHistoryCommand(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        String prefix = plugin.getMessages().getString("prefix", "&6[Magnata] &7");
        
        if (!sender.hasPermission(plugin.getMainConfig().getString("permissions.magnata_history", "magnata.history"))) {
            sender.sendMessage(prefix + plugin.getMessages().getString("errors.no_permission", "&cVocê não tem permissão!"));
            return true;
        }

        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        List<String> header = plugin.getMessages().getStringList("history.header");
        String entryFormat = plugin.getMessages().getString("history.entry");
        List<String> footer = plugin.getMessages().getStringList("history.footer");

        header.forEach(line -> sender.sendMessage(line.replace("%prefix%", prefix)));

        for (int i = 0; i < history.size(); i++) {
            MagnataRecord record = history.get(i);
            String entry = entryFormat
                    .replace("%position%", String.valueOf(i + 1))
                    .replace("%player%", record.getPlayerName())
                    .replace("%balance%", String.format("%,.2f", record.getBalance()))
                    .replace("%date%", record.getFormattedDate());
            sender.sendMessage(entry);
        }

        footer.forEach(msg -> sender.sendMessage(msg
                .replace("%total%", String.valueOf(history.size()))
                .replace("%prefix%", prefix)));
        
        return true;
    }
}
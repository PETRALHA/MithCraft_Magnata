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
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.magnata_history", "magnata.history"))) {
            sender.sendMessage(plugin.getMessages().getString("prefix") + "§cVocê não tem permissão para ver o histórico.");
            return true;
        }

        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        List<String> header = plugin.getConfig().getStringList("messages.history.header");
        String entryFormat = plugin.getConfig().getString("messages.history.entry");
        List<String> footer = plugin.getConfig().getStringList("messages.history.footer");

        // Envia cabeçalho
        header.forEach(sender::sendMessage);

        // Envia entradas
        for (int i = 0; i < history.size(); i++) {
            MagnataRecord record = history.get(i);
            String entry = entryFormat
                    .replace("%position%", String.valueOf(i + 1))
                    .replace("%player%", record.getPlayerName())
                    .replace("%balance%", String.format("%,.2f", record.getBalance()))
                    .replace("%date%", record.getFormattedDate());
            sender.sendMessage(entry);
        }

        // Envia rodapé
        footer.forEach(msg -> sender.sendMessage(msg.replace("%total%", String.valueOf(history.size()))));

        return true;
    }
}
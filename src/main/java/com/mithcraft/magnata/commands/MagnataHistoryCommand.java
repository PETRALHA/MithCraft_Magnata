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
        // Verificação de permissão
        if (!sender.hasPermission(plugin.getConfig().getString("permissions.magnata_history", "magnata.history"))) {
            sender.sendMessage(plugin.formatMessage(plugin.getMessages().getString("errors.no_permission")));
            return true;
        }

        // Obter histórico
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        
        // Verificar se há histórico
        if (history.isEmpty()) {
            sender.sendMessage(plugin.formatMessage("&eNenhum magnata registrado no histórico ainda."));
            return true;
        }

        // Enviar cabeçalho
        for (String line : plugin.getMessages().getStringList("history.header")) {
            sender.sendMessage(plugin.formatMessage(line));
        }

        // Enviar entradas do histórico
        String entryFormat = plugin.getMessages().getString("history.entry");
        int maxEntries = plugin.getConfig().getInt("settings.max_history_size", 10);
        
        for (int i = 0; i < Math.min(history.size(), maxEntries); i++) {
            MagnataRecord record = history.get(i);
            String formattedEntry = entryFormat
                .replace("%position%", String.valueOf(i + 1))
                .replace("%player%", record.getPlayerName())
                .replace("%balance%", plugin.formatCurrency(record.getBalance()))
                .replace("%date%", record.getFormattedDate());
            
            sender.sendMessage(plugin.formatMessage(formattedEntry));
        }

        // Enviar rodapé
        for (String line : plugin.getMessages().getStringList("history.footer")) {
            sender.sendMessage(plugin.formatMessage(line.replace("%total%", String.valueOf(history.size()))));
        }
        
        return true;
    }
}
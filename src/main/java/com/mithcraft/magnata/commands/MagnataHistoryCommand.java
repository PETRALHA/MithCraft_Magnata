package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MagnataHistoryCommand implements CommandExecutor {
    private final MagnataPlugin plugin;
    private final String permission;
    private final int entriesPerPage;
    private final int maxPages;

    public MagnataHistoryCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
        this.permission = plugin.getConfig().getString("permissions.history", "magnata.history");
        this.entriesPerPage = plugin.getConfig().getInt("pagination.entries_per_page", 5);
        this.maxPages = plugin.getConfig().getInt("pagination.max_pages", 5);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            if (!checkPermission(sender)) {
                return true;
            }

            List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
            if (history.isEmpty()) {
                sendEmptyHistoryMessage(sender);
                return true;
            }

            int page = parsePageNumber(args);
            displayHistoryPage(sender, history, page);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao exibir histórico: " + e.getMessage());
            sender.sendMessage(plugin.formatMessage(getMessage("errors.command_failed")));
            return true;
        }
    }

    private int parsePageNumber(String[] args) {
        if (args.length == 0) return 1;
        
        try {
            int page = Integer.parseInt(args[0]);
            return Math.max(1, Math.min(page, maxPages));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void displayHistoryPage(CommandSender sender, List<MagnataRecord> history, int page) {
        int totalPages = (int) Math.ceil((double) history.size() / entriesPerPage);
        page = Math.min(page, totalPages);
        
        int startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, history.size());

        sendSection(sender, "history.header", page, totalPages);
        sendHistoryEntries(sender, history.subList(startIndex, endIndex), startIndex);
        sendSection(sender, "history.footer", page, totalPages);
    }

    private boolean checkPermission(CommandSender sender) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.formatMessage(getMessage("errors.no_permission")));
            return false;
        }
        return true;
    }

    private void sendEmptyHistoryMessage(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage(getMessage("history.empty")));
    }

    private void sendSection(CommandSender sender, String path, Object... replacements) {
        List<String> lines = plugin.getMessages().getStringList(path);
        if (lines.isEmpty()) return;

        lines.forEach(line -> {
            String formatted = plugin.formatMessage(line)
                .replace("%page%", String.valueOf(replacements[0]))
                .replace("%total_pages%", String.valueOf(replacements[1]));
            sender.sendMessage(formatted);
        });
    }

    private void sendHistoryEntries(CommandSender sender, List<MagnataRecord> entries, int startIndex) {
        String entryFormat = getMessage("history.entry");

        for (int i = 0; i < entries.size(); i++) {
            MagnataRecord record = entries.get(i);
            sender.sendMessage(formatHistoryEntry(entryFormat, record, startIndex + i + 1));
        }
    }

    private String formatHistoryEntry(String format, MagnataRecord record, int position) {
        return plugin.formatMessage(format)
            .replace("%position%", String.valueOf(position))
            .replace("%player%", record.getPlayerName())
            .replace("%balance%", plugin.formatCurrency(record.getBalance()))
            .replace("%date%", record.getFormattedDate());
    }

    private String getMessage(String path) {
        return plugin.getMessages().getString(path, "&cMensagem não configurada: " + path);
    }
}

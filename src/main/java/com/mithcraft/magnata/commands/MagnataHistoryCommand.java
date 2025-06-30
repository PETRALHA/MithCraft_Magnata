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
        this.entriesPerPage = plugin.getConfig().getInt("settings.pagination.entries_per_page", 5);
        this.maxPages = plugin.getConfig().getInt("settings.pagination.max_pages", 5);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }

        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        if (history.isEmpty()) {
            sendEmptyMessage(sender);
            return true;
        }

        int requestedPage = parseRequestedPage(args);
        displayHistory(sender, history, requestedPage);
        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(plugin.formatMessage("errors.no_permission"));
        return false;
    }

    private int parseRequestedPage(String[] args) {
        if (args.length == 0) return 1;
        
        try {
            return Math.max(1, Math.min(Integer.parseInt(args[0]), maxPages));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void displayHistory(CommandSender sender, List<MagnataRecord> history, int requestedPage) {
        int totalPages = calculateTotalPages(history.size());
        int actualPage = Math.min(requestedPage, totalPages);
        
        sendHeader(sender, actualPage, totalPages);
        sendEntries(sender, history, actualPage);
        sendFooter(sender, actualPage, totalPages);
    }

    private int calculateTotalPages(int totalEntries) {
        return Math.max(1, (int) Math.ceil((double) totalEntries / entriesPerPage));
    }

    private void sendHeader(CommandSender sender, int page, int totalPages) {
        String header = plugin.formatMessage("commands.magnata.history.header")
            .replace("%page%", String.valueOf(page))
            .replace("%total%", String.valueOf(totalPages));
        sender.sendMessage(header);
    }

    private void sendEntries(CommandSender sender, List<MagnataRecord> history, int page) {
        int startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, history.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            MagnataRecord record = history.get(i);
            String entry = plugin.formatMessage("commands.magnata.history.entry")
                .replace("%position%", String.valueOf(i + 1))
                .replace("%player%", record.getPlayerName())
                .replace("%balance%", plugin.formatCurrency(record.getBalance()))
                .replace("%date%", record.getFormattedDate());
            sender.sendMessage(entry);
        }
    }

    private void sendFooter(CommandSender sender, int page, int totalPages) {
        String footer = plugin.formatMessage("commands.magnata.history.footer")
            .replace("%page%", String.valueOf(page))
            .replace("%total%", String.valueOf(totalPages));
        sender.sendMessage(footer);
    }

    private void sendEmptyMessage(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("commands.magnata.history.empty"));
    }
}
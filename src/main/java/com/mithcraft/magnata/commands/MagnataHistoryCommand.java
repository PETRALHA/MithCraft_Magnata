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
    private static final String HISTORY_PERMISSION = "magnata.history"; // Permissão hardcoded
    private final int entriesPerPage;
    private final int maxPages;

    public MagnataHistoryCommand(MagnataPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin não pode ser nulo");
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

        int requestedPage = parsePageNumber(args != null ? args : new String[0]);
        displayHistoryPage(sender, history, requestedPage);
        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(HISTORY_PERMISSION)) {
            return true;
        }
        sendMessage(sender, "errors.no_permission");
        return false;
    }

    private int parsePageNumber(String[] args) {
        if (args == null || args.length == 0) {
            return 1;
        }

        try {
            return Math.max(1, Math.min(Integer.parseInt(args[0]), maxPages));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void displayHistoryPage(CommandSender sender, List<MagnataRecord> history, int page) {
        int totalPages = calculateTotalPages(history.size());
        int actualPage = Math.min(page, totalPages);
        
        // Header
        sendMessage(sender, "commands.magnata.history.header",
            "{page}", String.valueOf(actualPage),
            "{total}", String.valueOf(totalPages));

        // Content
        String entryFormat = buildEntryFormat();
        int startIdx = (actualPage - 1) * entriesPerPage;
        int endIdx = Math.min(startIdx + entriesPerPage, history.size());
        
        for (int i = startIdx; i < endIdx; i++) {
            sendHistoryEntry(sender, entryFormat, history.get(i), i + 1);
        }

        // Footer
        sendMessage(sender, "commands.magnata.history.footer");
    }

    private String buildEntryFormat() {
        return plugin.colorize(
            plugin.getMessages().getString("commands.magnata.history.entry")
                .replace("{prefix}", getFormattedPrefix())
        );
    }

    private void sendHistoryEntry(CommandSender sender, String format, MagnataRecord record, int position) {
        sender.sendMessage(format
            .replace("{position}", String.valueOf(position))
            .replace("{player}", record.getPlayerName())
            .replace("{balance}", plugin.formatCurrency(record.getBalance()))
            .replace("{date}", record.getFormattedDate())
        );
    }

    private void sendEmptyMessage(CommandSender sender) {
        sendMessage(sender, "commands.magnata.history.empty");
    }

    private void sendMessage(CommandSender sender, String path, String... replacements) {
        String message = plugin.getMessages().getString(path);
        if (message == null || message.isEmpty()) return;

        String formatted = message.replace("{prefix}", getFormattedPrefix());
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formatted = formatted.replace(replacements[i], replacements[i+1]);
            }
        }
        sender.sendMessage(plugin.colorize(formatted));
    }

    private String getFormattedPrefix() {
        return plugin.colorize(plugin.getMessages().getString("formats.prefix", ""));
    }

    private int calculateTotalPages(int totalEntries) {
        return Math.max(1, (int) Math.ceil((double) totalEntries / entriesPerPage));
    }
}
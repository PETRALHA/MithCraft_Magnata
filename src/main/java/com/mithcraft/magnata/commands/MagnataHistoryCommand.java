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

        // Corrigido: Passa args corretamente (pode ser null)
        int requestedPage = parsePageNumber(args != null ? args : new String[0]);
        displayHistoryPage(sender, history, requestedPage);
        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        String noPermMessage = plugin.getMessages().getString("errors.no_permission")
            .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""));
        sender.sendMessage(noPermMessage);
        return false;
    }

    private int parsePageNumber(String[] args) {
        // Corrigido: Verifica se args é vazio ou nulo
        if (args == null || args.length == 0) {
            return 1;
        }

        try {
            int page = Integer.parseInt(args[0]);
            return Math.max(1, Math.min(page, maxPages));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void displayHistoryPage(CommandSender sender, List<MagnataRecord> history, int page) {
        int totalPages = calculateTotalPages(history.size());
        int actualPage = Math.min(page, totalPages);
        
        // Header
        sender.sendMessage(plugin.colorize(
            plugin.getMessages().getString("commands.magnata.history.header")
                .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
                .replace("{page}", String.valueOf(actualPage))
                .replace("{total}", String.valueOf(totalPages))
        ));

        // Entries
        String entryFormat = plugin.colorize(
            plugin.getMessages().getString("commands.magnata.history.entry")
                .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
        );

        int startIndex = (actualPage - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, history.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            MagnataRecord record = history.get(i);
            sender.sendMessage(entryFormat
                .replace("{position}", String.valueOf(i + 1))
                .replace("{player}", record.getPlayerName())
                .replace("{balance}", plugin.formatCurrency(record.getBalance()))
                .replace("{date}", record.getFormattedDate())
            );
        }

        // Footer
        sender.sendMessage(plugin.colorize(
            plugin.getMessages().getString("commands.magnata.history.footer")
                .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""))
        ));
    }

    private int calculateTotalPages(int totalEntries) {
        return Math.max(1, (int) Math.ceil((double) totalEntries / entriesPerPage));
    }

    private void sendEmptyMessage(CommandSender sender) {
        String emptyMessage = plugin.getMessages().getString("commands.magnata.history.empty")
            .replace("{prefix}", plugin.getMessages().getString("formats.prefix", ""));
        sender.sendMessage(emptyMessage);
    }
}
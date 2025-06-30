package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MagnataExpansion extends PlaceholderExpansion {
    private final MagnataPlugin plugin;
    private final DateTimeFormatter dateFormatter;

    public MagnataExpansion(MagnataPlugin plugin) {
        this.plugin = plugin;
        this.dateFormatter = DateTimeFormatter.ofPattern(
            plugin.getMessages().getString("formats.date", "dd/MM/yyyy HH:mm"),
            Locale.forLanguageTag("pt-BR")
        );
    }

    @Override
    public @NotNull String getIdentifier() {
        return "magnata";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MithCraft";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @NotNull String getDescription() {
        return "Fornece placeholders para o sistema Magnata do MithCraft";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // Placeholders básicos
        switch (params.toLowerCase()) {
            case "name":
                return getCurrentValue(m -> m.getPlayerName(), "Nenhum");
            case "uuid":
                return getCurrentValue(m -> m.getPlayerUUID().toString(), "");
            case "balance":
                return getCurrentValue(m -> plugin.formatCurrency(m.getBalance()), "0.00");
            case "date":
                return getCurrentValue(m -> m.getDate().format(dateFormatter), "");
            case "count":
                return String.valueOf(plugin.getHistoryManager().getHistory().size() + 1);
        }

        // Placeholders de histórico (previous_)
        if (params.startsWith("previous_")) {
            return handlePreviousPlaceholder(params.substring(9));
        }

        // Placeholders de posição dinâmica (position_X, balance_X, date_X)
        if (params.startsWith("position_") || params.startsWith("balance_") || params.startsWith("date_")) {
            return handlePositionPlaceholder(params);
        }

        // Placeholders formatados (top_X_line)
        if (params.startsWith("top_") && params.endsWith("_line")) {
            return handleTopLinePlaceholder(params);
        }

        // Placeholder de posição do jogador
        if (params.equals("player_position") && player != null) {
            int position = plugin.getHistoryManager().getPlayerPosition(player.getUniqueId());
            return position > 0 ? String.valueOf(position) : "N/A";
        }

        return null;
    }

    private String getCurrentValue(java.util.function.Function<MagnataRecord, String> mapper, String defaultValue) {
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        return current != null ? mapper.apply(current) : defaultValue;
    }

    private String handlePreviousPlaceholder(String subParam) {
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();
        if (history.isEmpty()) return "";

        MagnataRecord previous = history.get(0);
        switch (subParam.toLowerCase()) {
            case "name": return previous.getPlayerName();
            case "uuid": return previous.getPlayerUUID().toString();
            case "balance": return plugin.formatCurrency(previous.getBalance());
            case "date": return previous.getDate().format(dateFormatter);
            default: return "";
        }
    }

    private String handlePositionPlaceholder(String params) {
        try {
            String[] parts = params.split("_");
            if (parts.length != 2) return null;

            String type = parts[0];
            int position = Integer.parseInt(parts[1]);

            switch (type.toLowerCase()) {
                case "position":
                    return plugin.getHistoryManager().getPlayerAtPosition(position);
                case "balance":
                    return plugin.formatCurrency(plugin.getHistoryManager().getBalanceAtPosition(position));
                case "date":
                    return plugin.getHistoryManager().getDateAtPosition(position);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String handleTopLinePlaceholder(String params) {
        try {
            String positionStr = params.substring(4, params.length() - 5); // extrai o número de "top_X_line"
            int position = Integer.parseInt(positionStr);
            return plugin.getHistoryManager().getFormattedTopLine(position);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
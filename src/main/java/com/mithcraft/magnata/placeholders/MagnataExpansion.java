package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MagnataExpansion extends PlaceholderExpansion {
    private final MagnataPlugin plugin;

    public MagnataExpansion(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "magnata";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
        List<MagnataRecord> history = plugin.getHistoryManager().getHistory();

        // Placeholder principal (magnata atual)
        if (params.isEmpty() || params.equalsIgnoreCase("current")) {
            return current != null ? current.getPlayerName() : "Nenhum";
        }

        // Placeholders de histórico por posição
        if (params.startsWith("rank_")) {
            try {
                int position = Integer.parseInt(params.split("_")[1]);
                if (position >= 1 && position <= history.size()) {
                    return history.get(position - 1).getPlayerName();
                }
            } catch (Exception e) {
                return "Posição inválida";
            }
            return "N/A";
        }

        // Placeholder do magnata anterior
        if (params.equalsIgnoreCase("old")) {
            return history.size() > 0 ? history.get(0).getPlayerName() : "Nenhum";
        }

        // Placeholders existentes
        switch (params.toLowerCase()) {
            case "name":
                return current != null ? current.getPlayerName() : "Nenhum";
            case "balance":
                return current != null ? String.format("%,.2f", current.getBalance()) : "0.00";
            case "balance_raw":
                return current != null ? String.valueOf(current.getBalance()) : "0";
            case "date":
                return current != null ? current.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            case "time":
                return current != null ? current.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
            case "datetime":
                return current != null ? current.getFormattedDate() : "N/A";
            case "uuid":
                return current != null ? current.getPlayerUUID().toString() : "N/A";
            default:
                return null; // Placeholder desconhecido
        }
    }
}
package com.mithcraft.magnata.placeholders;

import com.mithcraft.magnata.MagnataPlugin;
import com.mithcraft.magnata.models.MagnataRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MagnataExpansion extends PlaceholderExpansion {
    private final MagnataPlugin plugin;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MagnataExpansion(MagnataPlugin plugin) {
        this.plugin = plugin;
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
        return "Fornece placeholders sobre o magnata atual e histórico";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        try {
            MagnataRecord current = plugin.getHistoryManager().getCurrentMagnata();
            List<MagnataRecord> history = plugin.getHistoryManager().getHistory();

            // Placeholders principais
            switch (params.toLowerCase()) {
                case "name":
                    return current != null ? current.getPlayerName() : "Nenhum";
                case "uuid":
                    return current != null ? current.getPlayerUUID().toString() : "";
                case "balance":
                    return current != null ? plugin.formatCurrency(current.getBalance()) : "0.00";
                case "date":
                    return current != null ? current.getDate().format(dateFormatter) : "";
                case "time":
                    return current != null ? current.getDate().format(timeFormatter) : "";
            }

            // Placeholders de histórico
            if (params.startsWith("previous_")) {
                if (history.size() < 2) return "";
                MagnataRecord previous = history.get(1);
                switch (params.substring(9).toLowerCase()) {
                    case "name": return previous.getPlayerName();
                    case "uuid": return previous.getPlayerUUID().toString();
                    case "balance": return plugin.formatCurrency(previous.getBalance());
                }
            }

            // Placeholders por posição (rank_1, rank_2, etc)
            if (params.startsWith("rank_")) {
                try {
                    int position = Integer.parseInt(params.substring(5));
                    if (position > 0 && position <= history.size()) {
                        return history.get(position - 1).getPlayerName();
                    }
                } catch (NumberFormatException ignored) {}
            }

            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao processar placeholder '" + params + "': " + e.getMessage());
            return null;
        }
    }
}

package com.mithcraft.magnata.commands;

import com.mithcraft.magnata.MagnataPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MagnataTabCompleter implements TabCompleter {
    private final MagnataPlugin plugin;

    public MagnataTabCompleter(MagnataPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Adiciona subcomandos básicos
            completions.add("help");
            completions.add("history");
            completions.add("hist");
            completions.add("reload");

            // Filtra com base no que o jogador já digitou
            completions.removeIf(s -> !s.toLowerCase().startsWith(args[0].toLowerCase()));
        }

        return completions;
    }
}
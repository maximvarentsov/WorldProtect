package ru.gtncraft.worldprotect.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;

import java.util.Optional;

public class Commands {

    public static Optional<String> getParam(final String args[], final int index) throws CommandException {
        if (args[index] == null) {
            return Optional.empty();
        }
        return Optional.of(args[index]);
    }

    public static Optional<OfflinePlayer> getPlayer(final String args[], final int index) throws CommandException {

        if (args[index] == null) {
            return Optional.empty();
        }

        String name = args[index];

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (player == null) {
            return Optional.empty();
        }

        return Optional.of(player);
    }

}

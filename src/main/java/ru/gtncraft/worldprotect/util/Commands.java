package ru.gtncraft.worldprotect.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;

import java.util.Optional;

public class Commands {

    public static Optional<String> getParam(final String args[], final int index) throws CommandException {
        if (args[index] == null) {
            return Optional.empty();
        }
        return Optional.of(args[index]);
    }

    public static OfflinePlayer getPlayer(final String args[], final int index) throws CommandException {

        if (args[index] == null) {
            throw new CommandException(Config.getInstance().getMessage(Messages.error_input_player));
        }

        String name = args[index];

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (player.getFirstPlayed() == 0) {
            throw new CommandException(Config.getInstance().getMessage(Messages.error_player_not_found, name));
        }

        return player;
    }

}

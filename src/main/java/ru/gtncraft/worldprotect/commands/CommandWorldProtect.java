
package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.region.Flag;
import ru.gtncraft.worldprotect.util.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.gtncraft.worldprotect.util.Strings.partial;

public class CommandWorldProtect implements CommandExecutor, TabCompleter {

    private final ProtectionManager manager;
    private final WorldProtect plugin;
    private final Collection<String> commands = ImmutableList.of(
        "save", "info", "flag", "help"
    );

    public CommandWorldProtect(final WorldProtect plugin) {
        this.manager = plugin.getProtectionManager();
        this.plugin = plugin;
        plugin.getCommand("worldprotect").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String commandLabel, String[] args) {
        if (args.length > 1) {
            String lastArg = args[args.length - 1];
            String command = args[0].toLowerCase();
            switch (args.length) {
                case 2:
                    if (commands.contains(command) && !("list".equals(command) || "help".equals(command))) {
                        Collection<String> worlds = new ArrayList<>();
                        for (World world : Bukkit.getWorlds()) {
                            worlds.add(world.getName());
                        }
                        return partial(lastArg, worlds);
                    }
                    break;
                case 3:
                    switch (command) {
                        case "flag":
                            return partial(lastArg, ImmutableList.of("set"));
                    }
                    break;
                case 4:
                    if ("flag".equals(command)) {
                        return partial(lastArg, Flag.toArray());
                    }
                    break;
                case 5:
                    if ("flag".equals(command)) {
                        return partial(lastArg, ImmutableList.of("true", "false"));
                    }
                    break;
            }
        } else if (args.length <= 1) {
            return partial(args[0], commands);
        }
        return ImmutableList.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        boolean usage = false;
        try {
            switch (args[0].toLowerCase()) {
                case "save":
                    save(sender);
                    break;
                case "info":
                    info(sender, args);
                    break;
                case "flag":
                    switch (args[2]) {
                        case "set":
                            setFlag(sender, args);
                            break;
                    }
                    break;
                case "help":
                    usage = true;
                    break;
                default:
                    sender.sendMessage(Messages.get(Message.error_unknown_command, commandLabel));
                    break;
            }
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            sender.sendMessage(Messages.get(Message.error_unknown_command, commandLabel));
        }
        return !usage;
    }

    private void save(final CommandSender sender) throws CommandException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    try {
                        manager.save(world);
                    } catch (IOException ex) {
                        plugin.getLogger().severe(ex.getMessage());
                    }
                }
                sender.sendMessage(Messages.get(Message.success_saved));
            }
        });
    }

    private void info(final CommandSender sender, final String[] args) throws CommandException {
        World world;
        if (args.length > 1) {
            world = Bukkit.getWorld(args[1].toLowerCase());
            if (world == null) {
                throw new CommandException(Messages.get(Message.error_input_world_not_found, args[1]));
            }
        } else {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                throw new CommandException(Messages.get(Message.error_player_command));
            }
        }
        sender.sendMessage(Region.showFlags(manager.getWorldFlags(world)));
    }

    private void setFlag(final CommandSender sender, final String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_world_name));
        }

        if (args.length < 4) {
            throw new CommandException(Messages.get(Message.error_input_flag));
        }

        if (args.length < 5) {
            throw new CommandException(Messages.get(Message.error_input_flag_value));
        }

        String world = args[1];
        String flag = args[3];
        String value = args[4];

        if (Bukkit.getWorld(world.toLowerCase()) == null) {
            Messages.get(Message.error_input_world_not_found, world);
        }

        boolean valueFlag;
        // reverse values, prevent true
        switch (value.toLowerCase()) {
            case "true":
                valueFlag = false;
                break;
            case "false":
                valueFlag = true;
                break;
            default:
                throw new CommandException(Messages.get(Message.error_input_flag_invalid_value));
        }

        Flag prevent;
        try {
            prevent = Flag.valueOf(flag);
        } catch (IllegalArgumentException ex) {
            throw new CommandException(Messages.get(Message.error_input_flag_unknown, flag));
        }

        if (sender.hasPermission(Permission.admin)) {
            manager.setWorldFlag(world, prevent, valueFlag);
            String flagState = valueFlag ? Messages.get(Message.flag_true) : Messages.get(Message.flag_false);
            sender.sendMessage(Messages.get(Message.success_world_flag_set, flag, world, flagState));
        } else {
            throw new CommandException(Messages.get(Message.error_no_permission));
        }
    }
}


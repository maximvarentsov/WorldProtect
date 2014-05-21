package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.storage.JsonFile;
import ru.gtncraft.worldprotect.storage.ProtectedWorld;
import ru.gtncraft.worldprotect.storage.Storage;
import ru.gtncraft.worldprotect.storage.Types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.gtncraft.worldprotect.util.Commands.getParam;
import static ru.gtncraft.worldprotect.util.Strings.partial;

class CommandWorldProtect implements CommandExecutor, TabCompleter {

    final Config config;
    final ProtectionManager manager;
    final WorldProtect plugin;
    final Collection<String> commands = ImmutableList.of(
            "save", "convert", "info", "flag", "help"
    );

    public CommandWorldProtect(final WorldProtect plugin) {
        this.config = plugin.getConfig();
        this.manager = plugin.getProtectionManager();
        this.plugin = plugin;

        PluginCommand command = plugin.getCommand("worldprotect");
        command.setExecutor(this);
        command.setPermissionMessage(config.getMessage(Messages.error_no_permission));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String commandLabel, String[] args) {
        if (args.length > 1) {
            final String lastArg = args[args.length - 1];
            final String command = args[0].toLowerCase();
            switch (args.length) {
                case 2:
                    if (commands.contains(command) && !("list".equals(command) || "help".equals(command))) {
                        return partial(lastArg, Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
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
                        return partial(lastArg, Prevent.toArray());
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
        boolean help = false;
        try {
            switch (args[0].toLowerCase()) {
                case "save":
                    save(sender);
                    break;
                case "convert":
                    convert(sender);
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
                    help = true;
                    break;
                default:
                    sender.sendMessage(config.getMessage(Messages.error_unknown_command, commandLabel));
                    break;
            }
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            sender.sendMessage(config.getMessage(Messages.error_unknown_command, commandLabel));
        }
        return !help;
    }

    void save(final CommandSender sender) throws CommandException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getWorlds().forEach(manager::save));
        sender.sendMessage(config.getMessage(Messages.success_saved));
    }

    void convert(final CommandSender sender) throws CommandException {
        if (config.getStorage() == Types.file) {
            throw new CommandException("Only convert from " + Types.mongodb.name() + " to " + Types.file.name() + " support for now.");
        }
        final Storage storage = new JsonFile(plugin);
        Bukkit.getServer()
              .getWorlds()
              .stream()
              .filter(config::useRegions)
              .forEach(world -> storage.save(world, new ProtectedWorld(
                      manager.get(world).collect(Collectors.toList()), manager.getWorldFlags(world)
              )));
        sender.sendMessage(config.getMessage(Messages.success_region_converted, Types.mongodb.name(), Types.file.name()));
    }

    void info(final CommandSender sender, final String[] args) throws CommandException {
        World world;
        if (args.length > 0) {
            world = Bukkit.getWorld(args[1].toLowerCase());
            if (world == null) {
                throw new CommandException(config.getMessage(Messages.error_input_world_not_found, args[1]));
            }
        } else {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                throw new CommandException(config.getMessage(Messages.error_player_command));
            }
        }
        sender.sendMessage(config.getFlags(manager.getWorldFlags(world)));
    }

    void setFlag(final CommandSender sender, final String[] args) throws CommandException {

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_world_name)
        ));

        if (Bukkit.getWorld(name.toLowerCase()) == null) {
            config.getMessage(Messages.error_input_world_not_found, name);
        }

        String flag = getParam(args, 3).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_flag)
        ));

        String value = getParam(args, 4).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_flag_value)
        ));

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
                throw new CommandException(config.getMessage(Messages.error_input_flag_invalid_value));
        }

        Prevent prevent;
        try {
            prevent = Prevent.valueOf(flag);
        } catch (IllegalArgumentException ex) {
            throw new CommandException(config.getMessage(Messages.error_input_flag_unknown, flag));
        }

        if (sender.hasPermission(Permissions.admin)) {
            manager.setWorldFlag(name, prevent, valueFlag);
            String flagState = valueFlag ? config.getMessage(Messages.flag_true) : config.getMessage(Messages.flag_false);
            sender.sendMessage(config.getMessage(Messages.success_world_flag_set, flag, name, flagState));
        } else {
            throw new CommandException(config.getMessage(Messages.error_no_permission));
        }
    }
}

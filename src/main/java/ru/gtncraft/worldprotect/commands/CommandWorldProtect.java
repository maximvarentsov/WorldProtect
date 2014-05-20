package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.database.Types;

import java.util.List;

import static ru.gtncraft.worldprotect.util.Strings.partial;

class CommandWorldProtect implements CommandExecutor, TabCompleter {

    final Config config;
    final ProtectionManager regions;
    final WorldProtect plugin;

    public CommandWorldProtect(final WorldProtect plugin) {
        this.config = plugin.getConfig();
        this.regions = plugin.getProtectionManager();
        this.plugin = plugin;

        PluginCommand command = plugin.getCommand("worldprotect");
        command.setExecutor(this);
        command.setPermissionMessage(config.getMessage(Messages.error_no_permission));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String label, String[] args) {
        if (args.length <= 1) {
            return partial(args[0], ImmutableList.of("save", "convert"));
        }
        return ImmutableList.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            switch (args[0].toLowerCase()) {
                case "save":
                    commandSave(sender);
                    return true;
                case "convert":
                    commandConvert(sender);
                    return true;
            }
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        return false;
    }

    void commandSave(final CommandSender sender) throws CommandException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getWorlds().forEach(regions::save));
        sender.sendMessage(config.getMessage(Messages.success_region_saved));
    }

    void commandConvert(final CommandSender sender) throws CommandException {
        if (config.getStorage() == Types.file) {
            throw new CommandException("Only convert from " + Types.mongodb.name() + " to " + Types.file.name() + " support for now.");
        }
        final Storage storage = new JsonFile(plugin);
        Bukkit.getServer().getWorlds().stream().filter(config::useRegions).forEach(world -> storage.save(world, regions.get(world)));
        sender.sendMessage(config.getMessage(Messages.success_region_converted, Types.mongodb.name(), Types.file.name()));
    }
}

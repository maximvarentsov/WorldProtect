package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.RegionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.Storage;
import java.util.List;
import static ru.gtncraft.worldprotect.util.Strings.partial;

public class CommandWorldProtect implements CommandExecutor, TabCompleter {

    private final Config config;
    private final RegionManager regions;
    private final WorldProtect plugin;

    public CommandWorldProtect(final WorldProtect plugin) {
        this.config = plugin.getConfig();
        this.regions = plugin.getRegionManager();
        this.plugin = plugin;

        final PluginCommand command = plugin.getCommand("worldprotect");
        command.setExecutor(this);
        command.setPermissionMessage(config.getMessage(Messages.error_no_permission));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
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
                    return commandSave(sender);
                case "convert":
                    return commandConvert(sender);
            }
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        return false;
    }

    private boolean commandSave(final CommandSender sender) throws CommandException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    regions.save(world);
                }
            }
        });
        sender.sendMessage(config.getMessage(Messages.success_region_saved));
        return true;
    }
    // TODO: covert is a toggler
    private boolean commandConvert(final CommandSender sender) throws CommandException {
        if ("file".equals(config.getString("storage.type"))) {
            throw new CommandException("Only convert from mongodb to file support now.");
        }
        final Storage storage = new JsonFile(plugin);
        for (World world : Bukkit.getServer().getWorlds()) {
            if (config.useRegions(world)) {
                storage.save(world, regions.get(world));
            }
        }
        sender.sendMessage(config.getMessage(Messages.success_region_converted, "mongodb", "file"));
        return true;
    }
}

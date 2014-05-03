package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.database.Types;
import ru.gtncraft.worldprotect.region.Region;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        if (args.length > 1) {
            final String lastArg = args[args.length - 1];
            final String command = args[0].toLowerCase();
            switch (command) {
                case "tp":
                    return partial(lastArg, allRegions(sender));
            }
        } else if (args.length <= 1) {
            return partial(args[0], ImmutableList.of("save", "convert", "tp"));
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
                case "tp":
                    if (sender instanceof Player) {
                        commandTeleport((Player) sender, args[1]);
                        return true;
                    }
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
            throw new CommandException("Only convert from " + Types.mongodb.name() + " to " + Types.file.name() + " support now.");
        }
        final Storage storage = new JsonFile(plugin);
        Bukkit.getServer().getWorlds().stream().filter(config::useRegions).forEach(world -> storage.save(world, regions.get(world)));
        sender.sendMessage(config.getMessage(Messages.success_region_converted, Types.mongodb.name(), Types.file.name()));
    }

    void commandTeleport(final Player sender, final String name) throws CommandException {

        Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_not_found, name)
        ));

        if (isFree(region.getCuboid().getUpperSW())) {
            sender.teleport(center(region.getCuboid().getUpperSW()));
        } else if (isFree(region.getCuboid().getLowerNE())) {
            sender.teleport(center(region.getCuboid().getUpperSW()));
        } else {
            sender.teleport(center(region.getCuboid().getCenter()));
        }
    }

    boolean isFree(final Location location) {
        final int x = location.getBlockX();
        final int y = location.getBlockZ();
        final int z = location.getBlockZ();
        return location.getWorld().getBlockAt(x,y + 1,z).getType().equals(Material.AIR);
    }

    Location center(final Location location) {
        return new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
    }

    Collection<String> allRegions(final CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission(Permissions.admin)) {
                return regions.get(player.getWorld()).map(Region::getName).collect(Collectors.toList());
            }
        }
        return ImmutableList.of();
    }
}

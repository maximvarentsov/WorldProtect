package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.region.Region;

import java.util.ArrayList;
import java.util.Collection;
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
                    return commandSave(sender);
                case "convert":
                    return commandConvert(sender);
                case "tp":
                    if (sender instanceof Player) {
                        return commandTeleport((Player) sender, args[1]);
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

    private boolean commandTeleport(final Player sender, final String name) throws CommandException {
        final Region region = regions.get(sender.getWorld(), name);
        if (region == null) {
            throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
        }

        if (isFree(region.getCuboid().getUpperSW())) {
            sender.teleport(region.getCuboid().getUpperSW());
        } else if (isFree(region.getCuboid().getLowerNE())) {
            sender.teleport(region.getCuboid().getUpperSW());
        } else {
            sender.teleport(region.getCuboid().getCenter());
        }

        return true;
    }

    private boolean isFree(final Location location) {
        final int x = location.getBlockX();
        final int y = location.getBlockZ();
        final int z = location.getBlockZ();

        return location.getWorld().getBlockAt(x,y + 1,z).getType().equals(Material.AIR);
    }

    private Collection<String> allRegions(final CommandSender sender) {
        final Collection<String> result = new ArrayList<>();
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (player.hasPermission(Permissions.admin)) {
                for (Region region: regions.get(player.getWorld()).values()) {
                    result.add(region.getName());
                }
            }
        }
        return result;
    }
}

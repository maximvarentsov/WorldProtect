package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Region;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static ru.gtncraft.worldprotect.util.Strings.partial;

public class CommandRegion implements CommandExecutor, TabCompleter {

    private final Config config;
    private final RegionManager regions;
    private final WorldEditPlugin we;

    public CommandRegion(final WorldProtect plugin) {
        this.config = plugin.getConfig();
        this.regions = plugin.getRegionManager();
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        final PluginCommand command = plugin.getCommand("region");
        command.setExecutor(this);
        command.setPermissionMessage(config.getMessage(Messages.error_no_permission));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String label, String[] args) {

        Collection<String> commands = ImmutableList.of(
            "define", "delete", "addowner", "deleteowner", "addmemeber", "deletemember", "info", "list", "flag"
        );

        if (args.length > 1) {
            final String lastArg = args[args.length - 1];
            final String command = args[0].toLowerCase();
            switch (args.length) {
                case 2:
                    if (commands.contains(command) && !"list".equals(command)) {
                        return partial(lastArg, allRegions((Player) sender));
                    }
                    break;
                case 3:
                    final String region = args[1];
                    switch (command) {
                        case "deleteowner":
                            return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Roles.owner));
                        case "deletemember":
                            return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Roles.member));
                        case "addowner":
                        case "addmember":
                            // Remember that we can return null to default to online player name matching
                            return null;
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
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        if (!config.useRegions(player.getWorld())) {
            player.sendMessage(config.getMessage(Messages.error_regions_disabled, player.getWorld().getName()));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    return define(player, args);
                case "delete":
                    return delete(player, args);
                case "addowner":
                    return addPlayer(player, args, Roles.owner);
                case "deleteowner":
                    return removePlayer(player, args, Roles.owner);
                case "addmember":
                    return addPlayer(player, args, Roles.member);
                case "deletemember":
                    return removePlayer(player, args, Roles.member);
                case "info":
                    return info(player, args);
                case "list":
                    return list(player);
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            return flag(player, args);
                    }
            }
        } catch (CommandException ex) {
            player.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        return false;
    }

    private boolean define(final Player sender, final String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(config.getMessage(Messages.error_worldedit_not_found));
        }

        final Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(config.getMessage(Messages.error_region_selection));
        }

        final Location p1 = selection.getMinimumPoint();
        final Location p2 = selection.getMaximumPoint();
        final String name = getParam(args, 1, config.getMessage(Messages.error_input_region_name));

        if (regions.get(sender.getWorld(), name) != null) {
            throw new CommandException(config.getMessage(Messages.error_region_name_exists, name));
        }

        final Region region = new Region(p1, p2);
        region.setName(name);
        region.add(sender.getName(), Roles.owner);

        if (!sender.hasPermission(Permissions.admin)) {
            /**
             * Check region volume
             */
            final int maxVolume = config.getInt("region.maxVolume");
            if (region.volume() > maxVolume) {
                throw new CommandException(
                    config.getMessage(Messages.error_region_max_volume, maxVolume, region.volume())
                );
            }
            /**
             *  Check region have overlay with another.
             */
            for (Region overlay : regions.getOverlays(region)) {
                if (!overlay.contains(sender, Roles.owner)) {
                    throw new CommandException(config.getMessage(Messages.error_region_overlay));
                }
            }
            if (!sender.hasPermission(Permissions.moder)) {
                /**
                 * Check region per player limit.
                 */
                final int max = config.getInt("region.maxPerPlayer");
                final int total = regions.get(sender, Roles.owner).size();
                if (max > 0 && total >= max) {
                    throw new CommandException(
                        config.getMessage(Messages.error_region_created_max, max)
                    );
                }
            }
        }
        regions.add(sender.getWorld(), region);
        sender.sendMessage(config.getMessage(Messages.success_region_created, name));
        return true;
    }

    private boolean list(final Player sender) {
        sender.sendMessage(config.getMessage(Messages.region_own_list) + ":");
        for (Region region : regions.get(sender, Roles.owner)) {
            sender.sendMessage(config.getMessage(Messages.region_name) + ": " + region.getName() + " " + region.getSize());
        }
        return true;
    }

    private boolean info(final Player sender, final String[] args) throws CommandException {
        if (args.length == 2) {
            String name = args[1];
            Region region = regions.get(sender.getWorld(), name);
            if (region == null) {
                throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
            }
            sender.sendMessage(config.getMessage(region));
            return true;
        } else if (args.length == 1) {
            Collection<Region> regions = this.regions.get(sender.getLocation());
            sender.sendMessage(config.getMessage(regions));
            return true;
        }
        return false;
    }

    private boolean delete(final Player sender, final String[] args) throws CommandException {
        String name = getParam(args, 1, config.getMessage(Messages.error_input_region_name));
        Region region = regions.get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
        }

        checkPermission(sender, region);

        regions.delete(sender.getWorld(), name);
        sender.sendMessage(config.getMessage(Messages.success_region_deleted, name));
        return true;
    }

    private boolean flag(final Player sender, final String[] args) throws CommandException {
        String name = getParam(args, 1, config.getMessage(Messages.error_input_region_name));
        Region region = regions.get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
        }

        String flag = getParam(args, 3, config.getMessage(Messages.error_input_flag));
        String value = getParam(args, 4, config.getMessage(Messages.error_input_flag_value));
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

        boolean perms = false;

        if (sender.hasPermission(Permissions.admin)) {
            perms = true;
        } else if (sender.hasPermission(Permissions.moder) || region.contains(sender, Roles.owner)) {
            if (config.isAllowedFlag(prevent)) {
                perms = true;
            }
        }

        if (perms) {
            region.set(prevent, valueFlag);
            sender.sendMessage(config.getMessage(Messages.success_region_flag_set, flag, name));
        } else {
            throw new CommandException(config.getMessage(Messages.error_no_permission));
        }

        return true;
    }

    private boolean addPlayer(final Player sender, final String[] args, final Roles role) throws CommandException {
        String name = getParam(args, 1, config.getMessage(Messages.error_input_region_name));
        Region region = regions.get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
        }

        String player = getParam(args, 2, config.getMessage(Messages.error_input_player));

        checkPermission(sender, region);

        if (!region.add(player, role)) {
            throw new CommandException(config.getMessage(Messages.error_region_contains_player, player));
        }
        sender.sendMessage(config.getMessage(Messages.success_region_player_add, player, name));
        return true;
    }

    private boolean removePlayer(final Player sender, final String[] args, final Roles role) throws CommandException {
        String name = getParam(args, 1, config.getMessage(Messages.error_input_region_name));
        Region region = regions.get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(config.getMessage(Messages.error_input_region_not_found, name));
        }

        String player = getParam(args, 2, config.getMessage(Messages.error_input_player));

        checkPermission(sender, region);

        if (!region.remove(player, role)) {
            throw new CommandException(config.getMessage(Messages.error_region_player_exists, player, name));
        }

        sender.sendMessage(config.getMessage(Messages.success_region_player_delete, player, name));
        return true;
    }

    private String getParam(final String args[], final int index, final String message) throws CommandException {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CommandException(message);
        }
    }

    private void checkPermission(final Player sender, final Region region) throws CommandException {
        if (!(sender.hasPermission(Permissions.admin) || region.contains(sender, Roles.owner))) {
            throw new CommandException(config.getMessage(Messages.error_no_permission));
        }
    }

    private Collection<String> allRegions(final Player player) {
        Collection<String> result = new ArrayList<>();
        if (player.hasPermission(Permissions.admin)) {
            for (Region region: regions.get(player.getWorld()).values()) {
                result.add(region.getName());
            }
        } else {
            for (Region region : regions.get(player, Roles.owner)) {
                result.add(region.getName());
            }
        }
        return result;
    }

    private Collection<String> allRegionPlayers(final World world, final String name, Roles role) {
        Collection<String> result = new ArrayList<>();
        Region region = regions.get(world, name);
        if (region != null) {
            for (String player : region.get(role)) {
                result.add(player);
            }
        }
        return result;
    }
}

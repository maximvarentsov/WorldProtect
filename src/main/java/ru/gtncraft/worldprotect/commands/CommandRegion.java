package ru.gtncraft.worldprotect.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.Permissions;
import ru.gtncraft.worldprotect.Roles;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Region;

import java.util.List;

public class CommandRegion implements CommandExecutor {

    private final WorldProtect plugin;
    private final WorldEditPlugin we;
    private final int regionPerPlayer;
    private final int regionMaxVolume;

    public CommandRegion(final WorldProtect plugin) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        this.regionPerPlayer = plugin.getConfig().getInt("region.maxPerPlayer");
        this.regionMaxVolume = plugin.getConfig().getInt("region.maxVolume");

        final PluginCommand command = plugin.getCommand("region");
        command.setExecutor(this);
        command.setTabCompleter(new CompleterRegion(plugin));
        command.setPermissionMessage(plugin.getConfig().getMessage(Messages.error_no_permission));
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!plugin.getConfig().useRegions(player.getWorld())) {
            player.sendMessage(plugin.getConfig().getMessage(Messages.error_regions_disabled, player.getWorld().getName()));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    return commandDefine(player, args);
                case "delete":
                    return commandDelete(player, args);
                case "addowner":
                    return addPlayer(player, args, Roles.owner);
                case "deleteowner":
                    return removePlayer(player, args, Roles.owner);
                case "addmember":
                    return addPlayer(player, args, Roles.member);
                case "deletemember":
                    return removePlayer(player, args, Roles.member);
                case "info":
                    return commandInfo(player, args);
                case "list":
                    return commandList(player);
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            return commandFlagSet(player, args);
                    }

            }
        } catch (CommandException ex) {
            player.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        // show usage
        return false;
    }

    private boolean commandDefine(final Player sender, final String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_worldedit_not_found));
        }

        Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_selection));
        }

        Location p1 = selection.getMinimumPoint();
        Location p2 = selection.getMaximumPoint();

        String name = getParam(args, 1, plugin.getConfig().getMessage(Messages.error_input_region_name));

        if (plugin.getRegionManager().get(sender.getWorld(), name) != null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_name_exists, name));
        }

        Region region = new Region(p1, p2);
        region.setName(name);
        region.add(sender.getName(), Roles.owner);

        if (!sender.hasPermission(Permissions.admin)) {
            /**
             * Check region volume
             */
            if (region.volume() > regionMaxVolume) {
                throw new CommandException(
                    plugin.getConfig().getMessage(Messages.error_region_max_volume, regionMaxVolume, region.volume())
                );
            }
            /**
             *  Check region have overlay with another.
             */
            for (Region overlay : plugin.getRegionManager().getOverlays(region)) {
                if (!overlay.contains(sender, Roles.owner)) {
                    throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_overlay));
                }
            }
            /**
             * Check region per player limit.
             */
            int total = plugin.getRegionManager().get(sender, Roles.owner).size();
            if (regionPerPlayer > 0 && total >= regionPerPlayer) {
                throw new CommandException(
                    plugin.getConfig().getMessage(Messages.error_region_created_max, String.valueOf(regionPerPlayer))
                );
            }
        }
        plugin.getRegionManager().add(sender.getWorld(), region);
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_created, name));
        return true;
    }

    private boolean commandList(final Player sender) {
        sender.sendMessage(plugin.getConfig().getMessage(Messages.region_own_list) + ":");
        for (Region region : plugin.getRegionManager().get(sender, Roles.owner)) {
            sender.sendMessage(
                plugin.getConfig().getMessage(Messages.region_name) + ": " + region.getName() + " " + region.getSize()
            );
        }
        return true;
    }

    private boolean commandInfo(final Player sender, final String[] args) throws CommandException {
        if (args.length == 2) {
            String name = args[1];
            Region region = plugin.getRegionManager().get(sender.getWorld(), name);
            if (region == null) {
                throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_region_not_found, name));
            }
            sender.sendMessage(plugin.getConfig().getMessage(region));
            return true;
        } else if (args.length == 1) {
            List<Region> regions = plugin.getRegionManager().get(sender.getLocation());
            sender.sendMessage(plugin.getConfig().getMessage(regions));
            return true;
        }
        return false;
    }

    private boolean commandDelete(final Player sender, final String[] args) throws CommandException {
        String name = getParam(args, 1, plugin.getConfig().getMessage(Messages.error_input_region_name));
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_region_not_found, name));
        }

        checkPermission(sender, region);

        plugin.getRegionManager().delete(sender.getWorld(), name);
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_deleted, name));
        return true;
    }

    private boolean commandFlagSet(final Player sender, final String[] args) throws CommandException {
        String name = getParam(args, 1, plugin.getConfig().getMessage(Messages.error_input_region_name));
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_region_not_found, name));
        }

        String flag = getParam(args, 3, plugin.getConfig().getMessage(Messages.error_input_flag));
        String value = getParam(args, 4, plugin.getConfig().getMessage(Messages.error_input_flag_value));
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
                throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_flag_invalid_value));
        }

        if (!sender.hasPermission(Permissions.admin)) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_no_permission));
        }

        try {
            region.set(Prevent.valueOf(flag), valueFlag);
            sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_flag_set, flag, name));
        } catch (IllegalArgumentException ex) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_flag_unknown, flag));
        }

        return true;
    }

    private boolean addPlayer(final Player sender, final String[] args, final Roles role) throws CommandException {
        String name = getParam(args, 1, plugin.getConfig().getMessage(Messages.error_input_region_name));
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_region_not_found, name));
        }

        String player = getParam(args, 2, plugin.getConfig().getMessage(Messages.error_input_player));

        checkPermission(sender, region);

        if (!region.add(player, role)) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_contains_player, player));
        }
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_player_add, player, name));
        return true;
    }

    private boolean removePlayer(final Player sender, final String[] args, final Roles role) throws CommandException {
        String name = getParam(args, 1, plugin.getConfig().getMessage(Messages.error_input_region_name));
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_input_region_not_found, name));
        }

        String player = getParam(args, 2, plugin.getConfig().getMessage(Messages.error_input_player));

        checkPermission(sender, region);

        if (!region.remove(player, role)) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_player_exists, player, name));
        }

        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_player_delete, player, name));
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
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_protected));
        }
    }
}

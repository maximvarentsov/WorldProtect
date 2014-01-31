package ru.gtncraft.worldprotect;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.util.List;

public class Commands implements CommandExecutor {

    private final WorldProtect plugin;
    private final WorldEditPlugin we;
    private final int regionPerPlayer;


    private class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    public Commands(final WorldProtect plugin) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        this.regionPerPlayer = plugin.getConfig().getInt("region.maxPerPlayer", 8);
        this.plugin.getCommand("region").setExecutor(this);
        this.plugin.getCommand("region").setTabCompleter(new CommandsCompleter(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.use)) {
            player.sendMessage(plugin.getConfig().getMessage(Messages.error_no_permission));
            return true;
        }
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
                case "save":
                    return commandSave(player);
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            return commandFlagSet(player, args);
                    }
                // show usage for unknown command args.
                default:
                    return false;
            }
        } catch (CommandException ex) {
            player.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private boolean commandDefine(Player sender, String[] args) throws CommandException {
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
        region.setId(name);
        region.add(sender.getName(), Roles.owner);

        if (!sender.hasPermission(Permissions.admin)) {
            /**
             *  Check region have overlay with another.
             */
            for (Region overlay : plugin.getRegionManager().get(p1, p2)) {
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

    private boolean commandList(Player sender) {
        sender.sendMessage(plugin.getConfig().getMessage(Messages.region_own_list) + ":");
        for (Region region : plugin.getRegionManager().get(sender, Roles.owner)) {
            sender.sendMessage(
                plugin.getConfig().getMessage(Messages.region_name) + ": " + region.getId() + " " + region.getSize()
            );
        }
        return true;
    }

    private boolean commandInfo(Player sender, String[] args) throws CommandException {
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

    private boolean commandSave(Player sender) throws CommandException {
        if (!sender.hasPermission(Permissions.admin)) {
            return false;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    plugin.getRegionManager().save(world);
                }
            }
        });
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_saved));
        return true;
    }

    private boolean commandDelete(Player sender, String[] args) throws CommandException {
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

    private boolean commandFlagSet(Player sender, String[] args) throws CommandException {
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

    private boolean addPlayer(Player sender, String[] args, Roles role) throws CommandException {
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

    private boolean removePlayer(Player sender, String[] args, Roles role) throws CommandException {
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

    private String getParam(String args[], int index, String message) throws CommandException {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CommandException(message);
        }
    }

    private void checkPermission(Player sender, Region region) throws CommandException {
        if (!(sender.hasPermission(Permissions.admin) || region.contains(sender, Roles.owner))) {
            throw new CommandException(plugin.getConfig().getMessage(Messages.error_region_protected));
        }
    }
}

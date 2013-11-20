package ru.gtncraft.worldprotect;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;
import java.util.List;

final public class Commands implements CommandExecutor {

    final private WorldProtect plugin;
    final private WorldEditPlugin we;

    private class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    public Commands(WorldProtect plugin) {
        plugin.getCommand("region").setExecutor(this);
        this.plugin = plugin;
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if ( ! (commandSender instanceof Player) ) {
            return false;
        }
        Player player = (Player) commandSender;
        if ( ! player.hasPermission(plugin.PERMISSION_USE) ) {
            return false;
        }
        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    return commandDefine(player, args);
                case "del":
                case "delete":
                case "remove":
                    return commandDelete(player, args);
                case "addowner":
                    return addPlayer(player, args, Players.role.owner);
                case "delowner":
                case "deleteowner":
                case "removeowner":
                    return removePlayer(player, args, Players.role.owner);
                case "addmember":
                    return addPlayer(player, args, Players.role.member);
                case "delmember":
                case "deletemember":
                case "removemember":
                    return removePlayer(player, args, Players.role.member);
                case "info":
                    return commandInfo(player, args);
                case "list":
                    return commandList(player);
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            return commandFlagSet(player, args);
                    }
                // usage for unknow subcommands.
                default:
                    return false;
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private boolean commandDefine(Player sender, String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(Lang.WORLDEDIT_NOT_INSTALLED);
        }

        Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(Lang.WORLDEDIT_NO_SELECTION);
        }

        Location p1 = selection.getMinimumPoint();
        Location p2 = selection.getMaximumPoint();

        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);

        if (plugin.getRegionManager().get(sender.getWorld(), name) != null) {
            throw new CommandException(String.format(Lang.REGION_EXISTS, name));
        }

        Region region = new Region(p1, p2);
        region.setName(name);
        region.add(sender.getName(), Players.role.owner);
        /**
         *  Check region have overlay with another
         */
         if (!sender.hasPermission(plugin.PERMISSION_ADMIN)) {
            for (Region regionMin : plugin.getRegionManager().get(p1)) {
                if (!regionMin.is(sender, Players.role.owner)) {
                    throw new CommandException(Lang.REGION_OVERLAY_WITH_ANOTHER);
                }
            }
            for (Region regionMax : plugin.getRegionManager().get(p2)) {
                if (!regionMax.is(sender, Players.role.owner)) {
                    throw new CommandException(Lang.REGION_OVERLAY_WITH_ANOTHER);
                }
            }
         }
         plugin.getRegionManager().add(sender.getWorld(), region);
         sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_CREATED, name));
         return true;
    }

    private boolean commandDelete(Player sender, String[] args) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        if ((!region.is(sender, Players.role.owner)) || (!sender.hasPermission(plugin.PERMISSION_ADMIN))) {
            throw new CommandException(Lang.REGION_NO_PERMISSION);
        }

        plugin.getRegionManager().delete(sender.getWorld(), name);
        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_DELETED, name));
        return true;
    }

    private boolean commandList(Player sender) {
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_OWN_LIST);
        for (Region region : plugin.getRegionManager().get(sender, Players.role.owner)) {
            sender.sendMessage(ChatColor.GREEN + Lang.REGION_NAME + " " + region.getName() + " " + region);
        }
        return true;
    }

    private boolean commandInfo(Player sender, String[] args) throws CommandException {
        if (args.length == 2) {
            String name = args[1];
            Region region = plugin.getRegionManager().get(sender.getWorld(), name);
            if (region == null) {
                throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
            }
            showRegionInfo(sender, region);
            return true;
        } else if (args.length == 1) {
            List<Region> regions = plugin.getRegionManager().get(sender.getLocation());
            if (regions.size() == 0) {
                throw new CommandException(Lang.REGION_NOT_FOUND_IN_AREA);
            }
            for (Region region : regions) {
                showRegionInfo(sender, region);
            }
            return true;
        }
        return false;
    }

    private boolean commandFlagSet(Player sender, String[] args) throws CommandException {

        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String flag = getParam(args, 3, Lang.FLAG_MISSING);

        String value = getParam(args, 4, Lang.FLAG_NO_VALUE);
        boolean valueFlag;

        switch (value.toLowerCase()) {
            case "1":
            case "on":
            case "true":
                valueFlag = true;
                break;
            case "0":
            case "off":
            case "false":
                valueFlag = false;
                break;
            default:
                throw new CommandException(String.format(Lang.FLAG_INVALID_VALUE, value));
        }

        try {
            if ((!region.is(sender, Players.role.owner)) || (!sender.hasPermission(plugin.PERMISSION_ADMIN))) {
                throw new CommandException(Lang.REGION_NO_PERMISSION);
            }
            region.set(Flags.prevent.valueOf(flag), valueFlag);
            sender.sendMessage(ChatColor.GREEN + String.format(Lang.FLAG_CHANGED, flag, name));
        } catch (IllegalArgumentException ex) {
            throw new CommandException(String.format(Lang.FLAG_UNKNOWN, flag));
        }

        return true;
    }

    private boolean addPlayer(Player sender, String[] args, Players.role role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String player = getParam(args, 2, Lang.PLAYER_NAME_MISSING);

        if ((!region.is(sender, Players.role.owner)) || (!sender.hasPermission(plugin.PERMISSION_ADMIN))) {
            throw new CommandException(Lang.REGION_NO_PERMISSION);
        }

        if (!region.add(player, role)) {
            throw new CommandException(Lang.PLAYER_ALREADY_IN_REGION);
        }

        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_PLAYER_ADDED, player, name));
        return true;
    }

    private boolean removePlayer(Player sender, String[] args, Players.role role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String player = getParam(args, 2, Lang.PLAYER_NAME_MISSING);

        if ((!region.is(sender, Players.role.owner)) || (!sender.hasPermission(plugin.PERMISSION_ADMIN))) {
            throw new CommandException(Lang.REGION_NO_PERMISSION);
        }

        if (!region.remove(player, role)) {
            throw new CommandException(Lang.PLAYER_NOT_FOUND_IN_REGION);
        }

        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_PLAYER_DELETED, player, name));
        return true;
    }

    private void showRegionInfo(Player sender, Region region) {
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_NAME    + " " + region.getName());
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_SIZE    + " " + region);
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_OWNERS  + " " + region.get(Players.role.owner));
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_MEMBERS + " " + region.get(Players.role.member));
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_FLAGS);
    }

    private String getParam(String args[], int index, String message) throws CommandException {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CommandException(message);
        }
    }
}

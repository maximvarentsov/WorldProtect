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
import ru.gtncraft.worldprotect.Region.Region;
import java.util.List;

final public class Commands implements CommandExecutor {

    final private WorldProtect plugin;
    private WorldEditPlugin we;

    private class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    public Commands(WorldProtect plugin) {
        plugin.getCommand("region").setExecutor(this);
        this.plugin = plugin;
    }

    private String getParam(String args[], int index, String message) throws CommandException {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CommandException(message);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if ( ! (commandSender instanceof Player) ) {
            return false;
        }
        Player player = (Player) commandSender;
        if ( ! player.hasPermission("worldprotect.use") ) {
            return false;
        }
        if (args.length == 0) {
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
                    return addPlayer(player, args, "owner");
                case "delowner":
                case "deleteowner":
                case "removeowner":
                    return deletePlayer(player, args, "owner");
                case "addmember":
                    return addPlayer(player, args, "member");
                case "delmember":
                case "deletemember":
                case "removemember":
                    return deletePlayer(player, args, "member");
                case "info":
                    return commandInfo(player, args);
                case "list":
                    return commandList(player);
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + ex.getMessage());
        }
        return true;
    }

    private void getWorldEdit() throws CommandException {
        if (we == null) {
            we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        }
        if (we == null) {
            throw new CommandException(Lang.WORLDEDIT_NOT_INSTALLED);
        }
    }

    private boolean commandDefine(Player sender, String[] args) throws CommandException {
        if (args.length != 2) {
            return false;
        }
        getWorldEdit();
        Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(Lang.WORLDEDIT_NO_SELECTION);
        }

        Location p1 = selection.getMinimumPoint();
        Location p2 = selection.getMaximumPoint();

        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        /**
         * Region name exists
         */
        if (plugin.getRegionManager().getRegions(sender.getWorld()).get(name) == null) {
            Region region = new Region(p1, p2);
            region.setName(name);
            region.owners._add(sender.getName());
            /**
             *  Check region have overlay with another
             */
            if ( ! sender.hasPermission("worldprotect.admin") ) {
                for (Region regionMin : plugin.getRegionManager().getRegions(p1)) {
                    if ( ! regionMin.isOwner(sender) ) {
                        throw new CommandException(Lang.REGION_OVERLAY_WITH_ANOTHER);
                    }
                }
                for (Region regionMax : plugin.getRegionManager().getRegions(p2)) {
                    if ( ! regionMax.isOwner(sender) ) {
                        throw new CommandException(Lang.REGION_OVERLAY_WITH_ANOTHER);
                    }
                }
            }
            plugin.getRegionManager().getRegions(sender.getWorld()).add(region);
            sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessCreated(name));
        } else {
            throw new CommandException(Lang.regionExists(name));
        }
        return true;
    }

    private boolean commandDelete(Player sender, String[] args) throws CommandException {
        if (args.length != 2) {
            return false;
        }
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().getRegions(sender.getWorld()).get(name);
        if (region == null) {
            throw new CommandException(Lang.regionNotFound(name));
        } else {
            if (region.isOwner(sender)) {
                plugin.getRegionManager().delete(sender.getWorld(), name);
                sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessDeleted(name));
            } else {
                throw new CommandException(Lang.YOU_DONT_OWN_THIS_REGION);
            }
        }
        return true;
    }

    private boolean commandList(Player sender) {
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_OWN_LIST);
        for (Region region : plugin.getRegionManager().getRegions(sender.getWorld()).getOwned(sender)) {
            String message = Lang.REGION_NAME + " " + region.getName() + " ( min: " + region.p1 + " max: " + region.p2 + " )";
            sender.sendMessage(ChatColor.GREEN + message);
        }
        return true;
    }

    private boolean addPlayer(Player sender, String[] args, String role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().getRegions(sender.getWorld()).get(name);
        if (region == null) {
            throw new CommandException(Lang.regionNotFound(name));
        } else {
            if (region.isOwner(sender)) {
                switch (role) {
                    case "member":
                        String member = getParam(args, 2, Lang.REGION_MISSING_MEMBER);
                        if (region.members.has(member)) {
                            throw new CommandException(Lang.PLAYER_ALREADY_IN_REGION);
                        }
                        region.members._add(member);
                        sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessMemberAdded(member, name));
                        break;
                    case "owner":
                        String owner = getParam(args, 2, Lang.REGION_MISSING_OWNER);
                        if (region.owners.has(owner)) {
                            throw new CommandException(Lang.PLAYER_ALREADY_IN_REGION);
                        }
                        region.owners._add(owner);
                        sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessOwnerAdded(owner, name));
                        break;
                }
            } else {
                throw new CommandException(Lang.REGION_ERROR_MEMBER_ADD);
            }
        }
        return true;
    }

    private boolean deletePlayer(Player sender, String[] args, String role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().getRegions(sender.getWorld()).get(name);
        if (region == null) {
            throw new CommandException(Lang.regionNotFound(name));
        } else {
            if (region.isOwner(sender)) {
                switch (role) {
                    case "member":
                        String member = getParam(args, 2, Lang.REGION_MISSING_MEMBER);
                        if (!region.members.has(member)) {
                            throw new CommandException(Lang.PLAYER_NOT_FOUND_IN_REGION);
                        }
                        region.members._remove(member);
                        sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessMemberRemoved(member, name));
                        break;
                    case "owner":
                        String owner = getParam(args, 2, Lang.REGION_MISSING_OWNER);
                        if (!region.owners.has(owner)) {
                            throw new CommandException(Lang.PLAYER_NOT_FOUND_IN_REGION);
                        }
                        region.owners._remove(owner);
                        sender.sendMessage(ChatColor.GREEN + Lang.regionSuccessOwnerRemoved(owner, name));
                        break;
                }
            } else {
                throw new CommandException(Lang.REGION_ERROR_MEMBER_DELETE);
            }
        }
        return true;
    }

    private boolean commandInfo(Player sender, String[] args) throws CommandException {
        if (args.length == 2) {
            String name = args[1];
            Region region = plugin.getRegionManager().getRegions(sender.getWorld()).get(name);
            if (region == null) {
                throw new CommandException(Lang.REGION_NOT_FOUND);
            } else {
                showRegionInfo(sender, region);
                return true;
            }
        } else if (args.length == 1) {
            List<Region> regions = plugin.getRegionManager().getRegions(sender.getLocation());
            if (regions.size() > 0) {
                for (Region region : regions) {
                    showRegionInfo(sender, region);
                    return true;
                }
            } else {
                throw new CommandException(Lang.REGION_NOT_FOUND_2);
            }
        }
        return false;
    }

    private void showRegionInfo(Player sender, Region region) {
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_NAME + " " + region.getName());
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_SIZE + " ( min: " + region.p1 + " max: " + region.p2 + " )");
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_OWNERS + " " + region.owners);
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_MEMBERS + " " + region.members);
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_FLAGS);
    }
}
